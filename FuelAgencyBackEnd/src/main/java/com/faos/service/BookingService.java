package com.faos.service;

import com.faos.exception.InvalidBookingOperationException;
import com.faos.exception.InvalidEntityException;
import com.faos.exception.ResourceNotFoundException;
import com.faos.model.Bill;
import com.faos.model.Booking;
import com.faos.model.User;
import com.faos.model.Cylinder;
import com.faos.enums.BookingStatus;
import com.faos.enums.DeliveryOption;
import com.faos.enums.PaymentStatus;
import com.faos.repositories.BookingRepository;
import com.faos.repositories.UserRepository;
import com.faos.repositories.CylinderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service

public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CylinderRepository cylinderRepository;

    // Inject our email sender
    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PdfService pdfService;

    private static final int CYLINDER_LIMIT = 6;

    // ✅ Fetch all bookings for a specific user
    public List<Booking> getBookingsByUser(Long userId) {
    	List<Booking> bookings = bookingRepository.findByUser_UserId(userId);
        // ✅ Ensure each booking loads its bill (Hibernate Lazy Loading Fix)
        for (Booking booking : bookings) {
            if (booking.getBill() != null) {
                booking.getBill().getTotalPrice(); // Force load bill to avoid LazyInitializationException
            }
        }
        
        return bookings;
    }

    // ✅ Get all bookings (ensuring Bills are eagerly fetched)
    public List<Booking> getAllBooking() {
        return bookingRepository.findAllWithBill();
    }

    // ✅ Get booking by ID
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }
    
    // Add to BookingService.java
    public boolean isUserEligibleForBooking(Long userId) {
        List<Booking> recentBookings = bookingRepository.findMostRecentBookingsByUser(userId);
        
        if (recentBookings.isEmpty()) {
            // No previous bookings, so eligible
            return true;
        }
        
        // Get the most recent booking (sorted in descending order by booking date)
        Booking latestBooking = recentBookings.get(0);
        LocalDate lastBookingDate = latestBooking.getBookingDate();
        LocalDate thirtyDaysAfter = lastBookingDate.plusDays(30);
        
        // Check if today is after the 30-day period
        return LocalDate.now().isAfter(thirtyDaysAfter);
    }

    // ✅ Add new booking (send email after successful save)
    @Transactional
    public Booking addBooking(Long userId, int cylinderId, Booking booking) {
        // 1) Fetch User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // 2) Fetch Cylinder
        Cylinder cylinder = cylinderRepository.findById(cylinderId)
                .orElseThrow(() -> new ResourceNotFoundException("Cylinder not found with ID: " + cylinderId));

        // Check if user is eligible for a new booking
        if (!isUserEligibleForBooking(userId)) {
            throw new InvalidBookingOperationException("You can only create a new booking 30 days after your previous booking.");
        }        
        
        // 3) Check if Cylinder is Already Booked
        if (cylinder.getCylinderStatus() == "Booked") {
            throw new InvalidBookingOperationException("Cylinder is already booked!");
        }

        // 4) Assign User & Cylinder
        booking.setUser(user);
        booking.setCylinder(cylinder);

        // 5) Update Cylinder Status
        cylinder.setCylinderStatus("Booked");
        cylinderRepository.save(cylinder);

        // 6) Set default values if not provided
        booking.setBookingDate(LocalDate.now());
        booking.setCylinderCount(1);
        booking.setPaymentStatus(
                booking.getPaymentStatus() != null
                        ? booking.getPaymentStatus()
                        : PaymentStatus.PAID
        );
        booking.setBookingStatus(
                booking.getBookingStatus() != null
                        ? booking.getBookingStatus()
                        : BookingStatus.PENDING
        );

        // 7) Validate Delivery Option
        DeliveryOption deliveryOption = booking.getDeliveryOption();
        if (deliveryOption == null) {
            throw new InvalidBookingOperationException("Delivery option must be specified.");
        }
        switch (deliveryOption) {
            case STANDARD -> booking.setDeliveryDate(booking.getBookingDate().plusDays(3));
            case EXPRESS -> booking.setDeliveryDate(booking.getBookingDate().plusDays(2));
            case SAME_DAY -> booking.setDeliveryDate(booking.getBookingDate());
            case SCHEDULED -> {
                if (booking.getDeliveryDate() == null || booking.getDeliveryDate().isBefore(booking.getBookingDate())) {
                    throw new InvalidBookingOperationException("Scheduled delivery requires a valid future date.");
                }
            }
        }

        // 8) Check if Bill already exists, else create one
        Bill bill = booking.getBill();
        if (bill == null) {
            bill = new Bill();
            bill.setPrice(BigDecimal.valueOf(1000));
            bill.setGst(BigDecimal.valueOf(50));
            bill.setDeliveryCharge(BigDecimal.valueOf(100));

            // Count previous bookings in the same year
            int bookedThisYear = bookingRepository.countCylindersBookedThisYear(booking.getBookingDate().getYear());
            boolean exceededLimit = (bookedThisYear >= CYLINDER_LIMIT);

            bill.calculateTotalPrice(booking.getCylinderCount(), exceededLimit);
        }
        booking.setBill(bill);
        bill.setBooking(booking);

        // 9) Save booking (cascades to Bill)
        Booking savedBooking = bookingRepository.save(booking);

        // 10) Send confirmation email
        sendBookingConfirmationEmail(savedBooking);

        return savedBooking;
    }

    // ✅ Update Booking
    @Transactional
    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking booking = getBookingById(id);

        // Allowed updates
        booking.setTimeSlot(updatedBooking.getTimeSlot());
        booking.setDeliveryOption(updatedBooking.getDeliveryOption());
        booking.setPaymentMode(updatedBooking.getPaymentMode());
        booking.setPaymentStatus(updatedBooking.getPaymentStatus());
        booking.setBookingStatus(updatedBooking.getBookingStatus());

        return bookingRepository.save(booking);
    }

    // ✅ Delete Booking
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        Cylinder cylinder = booking.getCylinder();
        if (cylinder != null) {
            cylinder.setCylinderStatus("Available");
            cylinderRepository.save(cylinder);
        }
        bookingRepository.delete(booking);
    }

    // ✅ Cancel Booking (send cancellation email)
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        // Check if the booking is already cancelled
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingOperationException("Booking is already cancelled.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Revert the cylinder's status to AVAILABLE
        Cylinder cylinder = booking.getCylinder();
        cylinder.setCylinderStatus("Available");
        cylinderRepository.save(cylinder);

        Booking savedBooking = bookingRepository.save(booking);

        // Send cancellation email
        sendBookingCancellationEmail(savedBooking);

        return savedBooking;
    }

    @Transactional
    public Booking markBookingAsDelivered(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new InvalidEntityException("Booking not found with ID: " + bookingId));
        
        booking.setBookingStatus(BookingStatus.DELIVERED);
        return bookingRepository.save(booking);
    }

    // --------------------------------
    // HELPER METHODS FOR EMAIL NOTICES
    // --------------------------------

    private void sendBookingConfirmationEmail(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }
        String email = booking.getUser().getEmail();
        String subject = "Booking Confirmation - ID: " + booking.getBookingId();
        String body = "Dear " + booking.getUser().getFirstName() + ",\n\n"
                + "Thank you for your booking!\n\n"
                + "Booking ID: " + booking.getBookingId() + "\n"
                + "Booking Date: " + booking.getBookingDate() + "\n"
                + "Delivery Date: " + booking.getDeliveryDate() + "\n"
                + "Payment Status: " + booking.getPaymentStatus() + "\n"
                + "Cylinder Type: " + booking.getCylinder().getType() + "\n\n"
                + "Regards,\nFuel Pro Management System";
        try {
            // Generate PDF
            byte[] pdfBytes = pdfService.generateBookingBillPdf(booking);
            
            // Send email with attachment
            emailSenderService.sendEmailWithAttachment(
                email, 
                subject, 
                body, 
                pdfBytes, 
                "booking_invoice_" + booking.getBookingId() + ".pdf", 
                "application/pdf"
            );
        } catch (IOException e) {
            // Handle exception - fallback to email without attachment
            emailSenderService.sendEmail(email, subject, body);
            // Log error
            System.err.println("Failed to generate PDF: " + e.getMessage());
        }
    }

    private void sendBookingCancellationEmail(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }
        String email = booking.getUser().getEmail();
        String subject = "Booking Cancelled - ID: " + booking.getBookingId();
        String body = "Hello " + booking.getUser().getFirstName() + ",\n\n"
                + "Your booking (ID: " + booking.getBookingId() + ") has been cancelled.\n"
                + "If you have any questions, please contact support.\n\n"
                + "Regards,\nFuel Pro Management System";

        emailSenderService.sendEmail(email, subject, body);
    }

}

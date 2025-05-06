package com.faos.service;

import com.faos.exception.ResourceNotFoundException;
import com.faos.model.Bill;
import com.faos.model.Booking;
import com.faos.model.Supplier;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

        @Autowired
    private BookingService bookingService;

    /**
     * Generate a PDF bill for a given booking
     * 
     * @param bookingId The ID of the booking
     * @return ResponseEntity containing the PDF as byte array
     */
    public ResponseEntity<byte[]> generateBookingBillPdfResponse(Long bookingId) {
        try {
            // Get the booking
            Booking booking = bookingService.getBookingById(bookingId);
            
            // Generate PDF
            byte[] pdfContent = generateBookingBillPdf(booking);
            
            // Setup HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Booking-Invoice-" + booking.getBookingId() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public byte[] generateSuppliersPdf(List<Supplier> suppliers, String title) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Set font and font size for the title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 750); // Position the title
                contentStream.showText(title);
                contentStream.endText();

                // Set font and font size for the table headers
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700); // Start position for headers
                contentStream.showText("Supplier ID");
                contentStream.newLineAtOffset(80, 0); // Move to the next column
                contentStream.showText("Name");
                contentStream.newLineAtOffset(120, 0); // Move to the next column
                contentStream.showText("Contact Person");
                contentStream.newLineAtOffset(120, 0); // Move to the next column
                contentStream.showText("Email");
                contentStream.newLineAtOffset(150, 0); // Move to the next column
                contentStream.showText("Status");
                contentStream.endText();

                // Set font and font size for the table data
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                int y = 680; // Initial Y position for data rows
                for (Supplier supplier : suppliers) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y); // Start position for data
                    contentStream.showText(String.valueOf(supplier.getSupplierId()));
                    contentStream.newLineAtOffset(80, 0); // Move to the next column
                    contentStream.showText(supplier.getName());
                    contentStream.newLineAtOffset(120, 0); // Move to the next column
                    contentStream.showText(supplier.getContactPerson());
                    contentStream.newLineAtOffset(120, 0); // Move to the next column
                    contentStream.showText(supplier.getEmail());
                    contentStream.newLineAtOffset(150, 0); // Move to the next column
                    contentStream.showText(supplier.getStatus().toString());
                    contentStream.endText();
                    y -= 20; // Move to the next row
                }
            }

            // Save the PDF to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    public byte[] generateBookingBillPdf(Booking booking) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Set title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 750);
                contentStream.showText("Booking Invoice - #" + booking.getBookingId());
                contentStream.endText();

                // Customer information
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Customer Details:");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Name: " + booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 685);
                contentStream.showText("Email: " + booking.getUser().getEmail());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 670);
                contentStream.showText("Phone: " + booking.getUser().getPhone());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 655);
                contentStream.showText("Address: " + booking.getUser().getAddress());
                contentStream.endText();

                // Booking details
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 625);
                contentStream.showText("Booking Details:");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 605);
                contentStream.showText("Booking ID: " + booking.getBookingId());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 590);
                contentStream.showText("Booking Date: " + booking.getBookingDate().format(dateFormatter));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 575);
                contentStream.showText("Delivery Date: " + booking.getDeliveryDate().format(dateFormatter));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 560);
                contentStream.showText("Delivery Option: " + booking.getDeliveryOption());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 545);
                contentStream.showText("Time Slot: " + booking.getTimeSlot());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 530);
                contentStream.showText("Cylinder Type: " + booking.getCylinder().getType());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 515);
                contentStream.showText("Cylinder Count: " + booking.getCylinderCount());
                contentStream.endText();

                // Bill details
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 485);
                contentStream.showText("Payment Details:");
                contentStream.endText();

                Bill bill = booking.getBill();
                
                // Create a table for bill items
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 465);
                contentStream.showText("Description");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText("Amount (INR)");
                contentStream.endText();

                // Draw a line
                contentStream.moveTo(50, 460);
                contentStream.lineTo(400, 460);
                contentStream.stroke();

                // Item rows
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                
                int y = 445;
                
                // Base price
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Cylinder Base Price");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(bill.getPrice().toString());
                contentStream.endText();
                
                y -= 15;
                
                // GST
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("GST");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(bill.getGst() != null ? bill.getGst().toString() : "0.0");
                contentStream.endText();
                
                y -= 15;
                
                // Delivery Charge
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Delivery Charge");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(bill.getDeliveryCharge() != null ? bill.getDeliveryCharge().toString() : "0.0");
                contentStream.endText();
                
                y -= 15;
                // CLE Charge
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Additional Charge");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(bill.getCLECharge().toString());
                contentStream.endText();
                
                y -= 15;
                
                // Draw a line
                contentStream.moveTo(50, y - 5);
                contentStream.lineTo(400, y - 5);
                contentStream.stroke();
                
                y -= 20;
                
                // Total
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Total Amount");
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(bill.getTotalPrice().toString());
                contentStream.endText();
                
                // Payment info
                y -= 30;
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Payment Status: " + booking.getPaymentStatus());
                contentStream.endText();
                
                y -= 15;
                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Payment Mode: " + booking.getPaymentMode());
                contentStream.endText();
                
                // Footer
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 100);
                contentStream.showText("For any queries, please contact our customer support.");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 85);
                contentStream.showText("Thank you for your using our services!");
                contentStream.endText();
            }

            // Save the PDF to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
}
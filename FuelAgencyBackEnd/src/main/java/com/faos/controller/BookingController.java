package com.faos.controller;

import com.faos.model.Booking;
import com.faos.service.BookingService;
import com.faos.service.PdfService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.util.List;


@CrossOrigin(origins = "http://localhost:1000") // ✅ Allow frontend requests
@RestController
@RequestMapping("/api/bookings")


public class BookingController {
    
    @Autowired
    private BookingService bookingService;

    @Autowired
    private PdfService pdfService;

    // ✅ Fetch all bookings for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUser(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    // ✅ Get all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBooking();
        return ResponseEntity.ok(bookings);
    }

    // ✅ Get a single booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/pdf/{bookingId}")
    public ResponseEntity<byte[]> downloadBookingInvoice(@PathVariable Long bookingId) {
        return pdfService.generateBookingBillPdfResponse(bookingId);
    }

    // ✅ Create a new booking (with User & Cylinder)
    @PostMapping("/user/{userId}/cylinder/{cylinderId}")
    public ResponseEntity<Booking> addBooking(@PathVariable Long userId,
                                              @PathVariable int cylinderId,
                                              @Valid @RequestBody Booking booking) {
        Booking created = bookingService.addBooking(userId, cylinderId, booking);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // ✅ Update an existing booking
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id,
                                                 @Valid @RequestBody Booking updatedBooking) {
        Booking updated = bookingService.updateBooking(id, updatedBooking);
        return ResponseEntity.ok(updated);
    }

    // ✅ Cancel an existing booking
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        Booking cancelled = bookingService.cancelBooking(id);
        return ResponseEntity.ok(cancelled);
    }

    @PutMapping("/{bookingId}/deliver")
    public ResponseEntity<?> markBookingAsDelivered(@PathVariable Long bookingId) {
        try {
            Booking updatedBooking = bookingService.markBookingAsDelivered(bookingId);
            return ResponseEntity.ok(updatedBooking);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking booking as delivered: " + e.getMessage());
        }
    }

    // ✅ Delete a booking by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully!");
    }


}

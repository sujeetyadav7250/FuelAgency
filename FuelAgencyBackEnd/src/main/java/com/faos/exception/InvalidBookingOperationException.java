package com.faos.exception;


/** Thrown when a booking-related operation fails due to a business rule (e.g., cylinder already booked). */
public class InvalidBookingOperationException extends RuntimeException {
    public InvalidBookingOperationException(String message) {
        super(message);
    }
}

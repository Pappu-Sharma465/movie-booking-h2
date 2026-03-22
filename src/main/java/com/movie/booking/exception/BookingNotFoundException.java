package com.movie.booking.exception;

public class BookingNotFoundException extends ResourceNotFoundException {
    public BookingNotFoundException(String bookingId) {
        super("Booking not found: " + bookingId);
    }
}

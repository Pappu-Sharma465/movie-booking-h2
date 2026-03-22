package com.movie.booking.exception;

public class SeatNotAvailableException extends BookingException {
    public SeatNotAvailableException(String seatId) {
        super("Seat is not available: " + seatId);
    }
}

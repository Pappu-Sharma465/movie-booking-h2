package com.movie.booking.exception;

public class TheatreNotFoundException extends ResourceNotFoundException {
    public TheatreNotFoundException(String theatreId) {
        super("Theatre not found: " + theatreId);
    }
}

package com.movie.booking.exception;

public class ShowNotFoundException extends ResourceNotFoundException {
    public ShowNotFoundException(String showId) {
        super("Show not found: " + showId);
    }
}

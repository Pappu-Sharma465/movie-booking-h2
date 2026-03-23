package com.movie.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkBookRequest {

    @NotEmpty(message = "bookings list must not be empty")
    @Valid
    private List<BookingItem> bookings;

    @Data
    public static class BookingItem {

        @NotBlank(message = "showId is required")
        private String showId;

        @NotBlank(message = "userId is required")
        private String userId;

        @NotEmpty(message = "seatIds must not be empty")
        private List<String> seatIds;
    }
}

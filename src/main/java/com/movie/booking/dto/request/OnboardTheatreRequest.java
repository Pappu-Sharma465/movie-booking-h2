package com.movie.booking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;


@Data
public class OnboardTheatreRequest {

    @NotBlank(message = "Theatre name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

    @Valid
    private List<ScreenRequest> screens;

    @Data
    public static class ScreenRequest {

        @NotBlank(message = "Screen name is required")
        private String name;

        @Min(value = 1, message = "Rows must be at least 1")
        private int rows;

        @Min(value = 1, message = "Seats per row must be at least 1")
        private int seatsPerRow;
    }
}

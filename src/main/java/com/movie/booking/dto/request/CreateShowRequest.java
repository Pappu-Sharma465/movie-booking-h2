package com.movie.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class CreateShowRequest {

    @NotBlank(message = "theatreId is required")
    private String theatreId;

    @NotBlank(message = "screenId is required")
    private String screenId;

    @NotBlank(message = "movieTitle is required")
    private String movieTitle;

    @NotBlank(message = "language is required")
    private String language;

    @NotBlank(message = "genre is required")
    private String genre;

    @NotBlank(message = "date is required (YYYY-MM-DD)")
    private String date;

    @NotBlank(message = "startTime is required (HH:MM)")
    private String startTime;

    @NotBlank(message = "endTime is required (HH:MM)")
    private String endTime;

    @Positive(message = "basePrice must be greater than 0")
    private double basePrice;
}

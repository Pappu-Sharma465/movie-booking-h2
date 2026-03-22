package com.movie.booking.dto.request;

import lombok.Data;

/**
 * All fields are optional — only non-null values are applied.
 * Supported updatable fields: movieTitle, language, genre, date,
 * startTime, endTime, basePrice.
 */
@Data
public class UpdateShowRequest {
    private String movieTitle;
    private String language;
    private String genre;
    private String date;
    private String startTime;
    private String endTime;
    private Double basePrice;
}

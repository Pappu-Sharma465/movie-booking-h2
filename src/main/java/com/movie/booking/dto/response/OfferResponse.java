package com.movie.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfferResponse {
    private String offerId;
    private String title;
    private String description;
    private String condition;
    private String applicableCities;
}

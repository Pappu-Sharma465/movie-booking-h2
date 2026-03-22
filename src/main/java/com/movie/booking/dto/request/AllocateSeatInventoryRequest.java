package com.movie.booking.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
public class AllocateSeatInventoryRequest {

    @Min(value = 1, message = "Rows must be at least 1")
    private int rows;

    @Min(value = 1, message = "Seats per row must be at least 1")
    private int seatsPerRow;
}

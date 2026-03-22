package com.movie.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Data
public class BookTicketsRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotEmpty(message = "At least one seatId must be provided")
    private List<String> seatIds;
}

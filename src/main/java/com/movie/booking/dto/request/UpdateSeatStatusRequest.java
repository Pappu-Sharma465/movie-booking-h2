package com.movie.booking.dto.request;

import com.movie.booking.entity.SeatStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSeatStatusRequest {

    @NotEmpty(message = "At least one seatId is required")
    private List<String> seatIds;

    @NotNull(message = "Status is required (AVAILABLE or BLOCKED)")
    private SeatStatus status;
}

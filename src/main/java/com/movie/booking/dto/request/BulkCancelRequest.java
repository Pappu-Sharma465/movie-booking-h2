package com.movie.booking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkCancelRequest {

    @NotEmpty(message = "bookingIds list must not be empty")
    private List<String> bookingIds;
}

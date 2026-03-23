package com.movie.booking.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BulkCancelResponse {

    private int    totalRequested;
    private int    succeeded;
    private int    failed;
    private List<CancelResult> results;

    @Data
    public static class CancelResult {
        private String  bookingId;
        private boolean success;
        private double  refundAmount;
        private String  errorMessage;
    }
}

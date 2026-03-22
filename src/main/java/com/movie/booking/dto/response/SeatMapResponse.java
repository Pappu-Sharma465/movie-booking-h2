package com.movie.booking.dto.response;

import com.movie.booking.entity.SeatStatus;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SeatMapResponse {

    private String showId;
    private String movieTitle;
    private String date;
    private String startTime;
    private double basePrice;
    private long   totalSeats;
    private long   availableSeats;
    private Map<String, List<SeatDetail>> rows;

    @Data
    public static class SeatDetail {
        private String     seatId;
        private SeatStatus status;

        public SeatDetail(String seatId, SeatStatus status) {
            this.seatId = seatId;
            this.status = status;
        }
    }
}

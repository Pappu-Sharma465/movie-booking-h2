package com.movie.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.booking.entity.Booking;
import com.movie.booking.entity.BookingStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class BookingResponse {

    private String        id;
    private String        showId;
    private String        theatreId;
    private String        userId;
    private List<String>  seats;
    private double        totalAmount;
    private BookingStatus status;
    private Instant       createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Instant       cancelledAt;
    private List<TicketDiscountResponse> discountBreakdown;

    @Data
    public static class TicketDiscountResponse {
        private int          ticketIndex;
        private double       originalPrice;
        private double       finalPrice;
        private List<String> discountReasons;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static BookingResponse from(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setShowId(b.getShowId());
        r.setTheatreId(b.getTheatreId());
        r.setUserId(b.getUserId());
        r.setSeats(b.getSeats());
        r.setTotalAmount(b.getTotalAmount());
        r.setStatus(b.getStatus());
        r.setCreatedAt(b.getCreatedAt());
        r.setCancelledAt(b.getCancelledAt());
        try {
            if (b.getDiscountBreakdownJson() != null && !b.getDiscountBreakdownJson().isBlank()) {
                r.setDiscountBreakdown(MAPPER.readValue(
                    b.getDiscountBreakdownJson(),
                    new TypeReference<List<TicketDiscountResponse>>() {}
                ));
            }
        } catch (Exception e) {
            r.setDiscountBreakdown(List.of());
        }
        return r;
    }
}

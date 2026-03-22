package com.movie.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Booking {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "show_id", nullable = false)
    private String showId;

    @Column(name = "theatre_id", nullable = false)
    private String theatreId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Comma-separated seat IDs (e.g. "A1,A2,A3").
     * Simple approach avoids a join table for a read-only list.
     */
    @Column(name = "seats", nullable = false, length = 1000)
    private String seatsRaw;

    /**
     * Comma-separated per-seat prices matching the seats order.
     */
    @Column(name = "per_seat_prices", nullable = false, length = 1000)
    private String perSeatPricesRaw;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "discount_breakdown", length = 4000)
    private String discountBreakdownJson;

    public Booking(String id, String showId, String theatreId, String userId,
                   List<String> seats, List<Double> perSeatPrices,
                   double totalAmount, String discountBreakdownJson) {
        this.id                    = id;
        this.showId                = showId;
        this.theatreId             = theatreId;
        this.userId                = userId;
        this.seatsRaw              = String.join(",", seats);
        this.perSeatPricesRaw      = perSeatPrices.stream()
                                        .map(String::valueOf)
                                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
        this.totalAmount           = totalAmount;
        this.discountBreakdownJson = discountBreakdownJson;
        this.status                = BookingStatus.CONFIRMED;
        this.createdAt             = Instant.now();
    }

    public List<String> getSeats() {
        if (seatsRaw == null || seatsRaw.isBlank()) return List.of();
        return List.of(seatsRaw.split(","));
    }

    public List<Double> getPerSeatPrices() {
        if (perSeatPricesRaw == null || perSeatPricesRaw.isBlank()) return List.of();
        return List.of(perSeatPricesRaw.split(",")).stream()
                   .map(Double::parseDouble).toList();
    }

    public void cancel() {
        this.status      = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }
}

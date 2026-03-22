package com.movie.booking.service;

import com.movie.booking.dto.response.OfferResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless discount engine — no DB dependency.
 *
 * Rules (additive on base price):
 *   1. Every 3rd ticket in a booking  →  50% off
 *   2. Afternoon show (12:00–17:00)   →  20% off
 */
@Service
public class OfferService {

    private static final int AFTERNOON_START = 720;   // 12:00
    private static final int AFTERNOON_END   = 1020;  // 17:00


    public record TicketPricing(
        int          ticketIndex,
        double       originalPrice,
        double       finalPrice,
        List<String> discountReasons
    ) {}

    public record PricingResult(
        List<Double>        perSeatPrices,
        double              total,
        List<TicketPricing> breakdown
    ) {}

       public PricingResult calculate(double basePrice, int seatCount, String startTime) {
        boolean afternoon = isAfternoonShow(startTime);

        List<Double>        perSeat   = new ArrayList<>();
        List<TicketPricing> breakdown = new ArrayList<>();
        double total = 0;

        for (int i = 1; i <= seatCount; i++) {
            double discAmt = 0;
            List<String> reasons = new ArrayList<>();

            if (i % 3 == 0) {
                discAmt += basePrice * 0.50;
                reasons.add("50% off (3rd ticket)");
            }
            if (afternoon) {
                discAmt += basePrice * 0.20;
                reasons.add("20% off (afternoon show)");
            }

            double finalPrice = Math.max(0, basePrice - discAmt);
            perSeat.add(finalPrice);
            total += finalPrice;
            breakdown.add(new TicketPricing(i, basePrice, finalPrice, reasons));
        }

        return new PricingResult(perSeat, total, breakdown);
    }

    public List<OfferResponse> listOffers(String city) {
        List<OfferResponse> all = List.of(
            new OfferResponse(
                "OFFER-001",
                "50% off on your 3rd Ticket",
                "Book 3 or more tickets and the 3rd one is half price.",
                "Every 3rd ticket in a single booking",
                "all"
            ),
            new OfferResponse(
                "OFFER-002",
                "20% off on Afternoon Shows",
                "All tickets for shows starting between 12:00 and 17:00 get a flat 20% discount.",
                "Show starts between 12:00-17:00",
                "all"
            )
        );

        if (city == null || city.isBlank()) return all;

        return all.stream()
                  .filter(o -> "all".equalsIgnoreCase(o.getApplicableCities())
                               || o.getApplicableCities().equalsIgnoreCase(city))
                  .toList();
    }

    public boolean isAfternoonShow(String startTime) {
        int mins = parseMinutes(startTime);
        return mins >= AFTERNOON_START && mins < AFTERNOON_END;
    }

    public static int parseMinutes(String time) {
        String[] p = time.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}

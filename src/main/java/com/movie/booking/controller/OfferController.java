package com.movie.booking.controller;

import com.movie.booking.dto.response.ApiResponse;
import com.movie.booking.dto.response.OfferResponse;
import com.movie.booking.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Offer discovery — returns active platform offers to B2C customers.
 *
 * GET  /api/offers     List all offers (optionally filtered by city)
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Offers (B2C)", description = "Discover active discount offers on the platform")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(
        summary     = "List active offers  [B2C]",
        description = "Returns all currently active discount offers. Pass an optional city " +
                      "parameter to see city-specific promotions. Platform offers: " +
                      "(1) 50% off on every 3rd ticket in a booking, " +
                      "(2) 20% off on all afternoon shows (12:00-17:00)."
    )
    public ResponseEntity<ApiResponse<List<OfferResponse>>> listOffers(
            @Parameter(description = "City name to filter city-specific offers (optional)")
            @RequestParam(required = false) String city) {

        List<OfferResponse> offers = offerService.listOffers(city);
        return ResponseEntity.ok(ApiResponse.ok(offers));
    }
}

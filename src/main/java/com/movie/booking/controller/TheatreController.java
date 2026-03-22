package com.movie.booking.controller;

import com.movie.booking.dto.request.AllocateSeatInventoryRequest;
import com.movie.booking.dto.request.OnboardTheatreRequest;
import com.movie.booking.dto.request.UpdateSeatStatusRequest;
import com.movie.booking.dto.response.ApiResponse;
import com.movie.booking.dto.response.TheatreResponse;
import com.movie.booking.entity.Theatre;
import com.movie.booking.service.TheatreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * B2B — Theatre partner management.
 *
 * POST   /api/b2b/theatres                              Onboard a theatre
 * GET    /api/b2b/theatres                              List all theatres
 * GET    /api/b2b/theatres/{theatreId}                  Get a theatre
 * POST   /api/b2b/shows/{showId}/seats/allocate         Allocate seat inventory
 * PATCH  /api/b2b/shows/{showId}/seats/status           Block / unblock seats
 */
@RestController
@RequestMapping("/api/b2b")
@RequiredArgsConstructor
@Tag(name = "Theatre Partner (B2B)", description = "Endpoints for theatre partner onboarding and management")
public class TheatreController {

    private final TheatreService theatreService;

    @PostMapping("/theatres")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Onboard a new theatre partner",
               description = "Registers a theatre with one or more screens onto the Movie platform.")
    public ResponseEntity<ApiResponse<TheatreResponse>> onboardTheatre(
            @Valid @RequestBody OnboardTheatreRequest req) {

        Theatre theatre = theatreService.onboard(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(TheatreResponse.from(theatre)));
    }

    @GetMapping("/theatres")
    @Operation(summary = "List all registered theatres")
    public ResponseEntity<ApiResponse<List<TheatreResponse>>> listTheatres() {
        List<TheatreResponse> theatres = theatreService.getAllTheatres()
                .stream().map(TheatreResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(theatres));
    }

    @GetMapping("/theatres/{theatreId}")
    @Operation(summary = "Get a theatre by ID")
    public ResponseEntity<ApiResponse<TheatreResponse>> getTheatre(
            @PathVariable String theatreId) {

        Theatre theatre = theatreService.getTheatre(theatreId);
        return ResponseEntity.ok(ApiResponse.ok(TheatreResponse.from(theatre)));
    }

    @PostMapping("/shows/{showId}/seats/allocate")
    @Operation(summary = "Allocate seat inventory for a show",
               description = "Creates or replaces the seat grid (rows × seatsPerRow). " +
                             "Blocked if any seats are already booked.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> allocateSeatInventory(
            @PathVariable String showId,
            @Valid @RequestBody AllocateSeatInventoryRequest req) {

        int total = theatreService.allocateSeatInventory(showId, req.getRows(), req.getSeatsPerRow());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "showId",     showId,
                "totalSeats", total,
                "message",    "Seat inventory allocated successfully"
        )));
    }

    @PatchMapping("/shows/{showId}/seats/status")
    @Operation(summary = "Block or unblock specific seats",
               description = "Sets listed seats to AVAILABLE or BLOCKED. " +
                             "Cannot change BOOKED seats — cancel the booking first.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSeatStatus(
            @PathVariable String showId,
            @Valid @RequestBody UpdateSeatStatusRequest req) {

        theatreService.updateSeatStatus(showId, req.getSeatIds(), req.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "showId",       showId,
                "updatedSeats", req.getSeatIds(),
                "newStatus",    req.getStatus()
        )));
    }
}

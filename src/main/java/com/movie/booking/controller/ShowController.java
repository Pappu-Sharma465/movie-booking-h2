package com.movie.booking.controller;

import com.movie.booking.dto.request.CreateShowRequest;
import com.movie.booking.dto.request.UpdateShowRequest;
import com.movie.booking.dto.response.ApiResponse;
import com.movie.booking.dto.response.SeatMapResponse;
import com.movie.booking.dto.response.ShowResponse;
import com.movie.booking.dto.response.ShowSummaryResponse;
import com.movie.booking.entity.Show;
import com.movie.booking.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Show management and browsing.
 *
 * B2B (theatre partner)
 *   POST    /api/b2b/shows                 Create a show
 *   PUT     /api/b2b/shows/{showId}        Update a show
 *   DELETE  /api/b2b/shows/{showId}        Delete a show
 *
 * B2C (end customer)
 *   GET     /api/shows                     Browse shows by movie + city + date
 *   GET     /api/shows/{showId}            Get show details
 *   GET     /api/shows/{showId}/seats      Seat map for a show
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Shows", description = "Show lifecycle management (B2B) and browsing (B2C)")
public class ShowController {

    private final ShowService showService;

    @PostMapping("/api/b2b/shows")
    @Operation(
        summary     = "Create a show  [B2B]",
        description = "Creates a new show for a registered theatre and screen. " +
                      "The seat grid is auto-initialised from the screen's row/column configuration."
    )
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @Valid @RequestBody CreateShowRequest req) {

        Show show = showService.createShow(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ShowResponse.from(show)));
    }

    @PutMapping("/api/b2b/shows/{showId}")
    @Operation(
        summary     = "Update a show  [B2B]",
        description = "Partially updates a show. Only non-null fields are applied. " +
                      "Updatable fields: movieTitle, language, genre, date, startTime, endTime, basePrice."
    )
    public ResponseEntity<ApiResponse<ShowResponse>> updateShow(
            @PathVariable String showId,
            @RequestBody UpdateShowRequest req) {

        Show show = showService.updateShow(showId, req);
        return ResponseEntity.ok(ApiResponse.ok(ShowResponse.from(show)));
    }

    @DeleteMapping("/api/b2b/shows/{showId}")
    @Operation(
        summary     = "Delete a show  [B2B]",
        description = "Permanently deletes a show. Blocked if confirmed bookings exist — " +
                      "cancel all bookings first."
    )
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteShow(
            @PathVariable String showId) {

        showService.deleteShow(showId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "showId",  showId,
                "message", "Show deleted successfully"
        )));
    }

    @GetMapping("/api/shows")
    @Operation(
        summary     = "Browse shows  [B2C]",
        description = "Find all shows for a movie in a city on a given date, sorted by start time. " +
                      "Language and genre are optional filters."
    )
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>> browseShows(
            @Parameter(description = "Movie title (partial match, case-insensitive)", required = true)
            @RequestParam String movie,

            @Parameter(description = "City name (case-insensitive)", required = true)
            @RequestParam String city,

            @Parameter(description = "Screening date — YYYY-MM-DD", required = true)
            @RequestParam String date,

            @Parameter(description = "Optional language filter")
            @RequestParam(required = false) String language,

            @Parameter(description = "Optional genre filter")
            @RequestParam(required = false) String genre) {

        List<ShowSummaryResponse> shows =
                showService.browseShows(movie, city, date, language, genre);
        return ResponseEntity.ok(ApiResponse.ok(shows));
    }

    @GetMapping("/api/shows/{showId}")
    @Operation(
        summary     = "Get a show by ID  [B2C]",
        description = "Returns full details of a single show including available seat count."
    )
    public ResponseEntity<ApiResponse<ShowResponse>> getShow(
            @PathVariable String showId) {

        Show show = showService.getShow(showId);
        return ResponseEntity.ok(ApiResponse.ok(ShowResponse.from(show)));
    }

    @GetMapping("/api/shows/{showId}/seats")
    @Operation(
        summary     = "Get seat map for a show  [B2C]",
        description = "Returns the full seat grid grouped by row label (A, B, C…). " +
                      "Each seat includes its status: AVAILABLE, BOOKED, or BLOCKED."
    )
    public ResponseEntity<ApiResponse<SeatMapResponse>> getSeatMap(
            @PathVariable String showId) {

        SeatMapResponse seatMap = showService.getSeatMap(showId);
        return ResponseEntity.ok(ApiResponse.ok(seatMap));
    }
}

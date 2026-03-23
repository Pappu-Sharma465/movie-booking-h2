package com.movie.booking.controller;

import com.movie.booking.dto.request.BookTicketsRequest;
import com.movie.booking.dto.request.BulkBookRequest;
import com.movie.booking.dto.request.BulkCancelRequest;
import com.movie.booking.dto.response.ApiResponse;
import com.movie.booking.dto.response.BookingResponse;
import com.movie.booking.dto.response.BulkCancelResponse;
import com.movie.booking.entity.Booking;
import com.movie.booking.service.BookingService;
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
 * Booking lifecycle — end-customer (B2C) operations.
 *
 * POST    /api/bookings/{showId}          Book seats for a show
 * DELETE  /api/bookings/{bookingId}       Cancel a booking
 * GET     /api/bookings/{bookingId}       Get a booking by ID
 * GET     /api/bookings/user/{userId}     List all bookings for a user
 * POST    /api/bookings/bulk              Bulk-book across multiple shows (atomic)
 * POST    /api/bookings/bulk-cancel       Bulk-cancel multiple bookings
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings (B2C)", description = "Ticket booking, bulk booking, cancellations, and booking history")
public class BookingController {

    private final BookingService bookingService;


    @PostMapping("/{showId}")
    @Operation(
        summary     = "Book tickets for a show  [B2C]",
        description = "Reserves the requested seats and applies any applicable offers " +
                      "(50% on every 3rd ticket, 20% for afternoon shows). " +
                      "All seats are validated atomically before any reservation is made."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> bookTickets(
            @Parameter(description = "Show ID to book tickets for", required = true)
            @PathVariable String showId,
            @Valid @RequestBody BookTicketsRequest req) {

        Booking booking = bookingService.book(showId, req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(BookingResponse.from(booking)));
    }

    @DeleteMapping("/{bookingId}")
    @Operation(
        summary     = "Cancel a booking  [B2C]",
        description = "Cancels a confirmed booking and returns all reserved seats to AVAILABLE. " +
                      "The full booking amount is flagged as a refund in the response."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelBooking(
            @Parameter(description = "Booking ID to cancel", required = true)
            @PathVariable String bookingId) {

        Booking booking = bookingService.cancel(bookingId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "bookingId",    booking.getId(),
                "status",       booking.getStatus(),
                "refundAmount", booking.getTotalAmount(),
                "cancelledAt",  booking.getCancelledAt().toString()
        )));
    }

    @GetMapping("/{bookingId}")
    @Operation(
        summary     = "Get a booking by ID  [B2C]",
        description = "Returns full booking details including per-ticket discount breakdown."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable String bookingId) {

        Booking booking = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.ok(BookingResponse.from(booking)));
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary     = "List all bookings for a user  [B2C]",
        description = "Returns all bookings (confirmed and cancelled) for the given user ID, " +
                      "ordered by creation time ascending."
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {

        List<BookingResponse> bookings = bookingService.listUserBookings(userId)
                .stream().map(BookingResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }

    @PostMapping("/bulk")
    @Operation(
            summary     = "Bulk-book across multiple shows  [B2C]",
            description = "Books seats across multiple shows in a single atomic operation. " +
                    "If any individual booking fails the entire batch is rolled back " +
                    "and no seats are reserved."
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> bulkBook(
            @Valid @RequestBody BulkBookRequest req) {

        List<Booking> bookings = bookingService.bulkBook(req);
        List<BookingResponse> response = bookings.stream()
                .map(BookingResponse::from)
                .toList();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PostMapping("/bulk-cancel")
    @Operation(
            summary     = "Bulk-cancel multiple bookings  [B2C]",
            description = "Cancels multiple bookings in one request. Each cancellation is attempted " +
                    "independently — partial failures are reported per booking without stopping the batch."
    )
    public ResponseEntity<ApiResponse<BulkCancelResponse>> bulkCancel(
            @Valid @RequestBody BulkCancelRequest req) {

        BulkCancelResponse result = bookingService.bulkCancel(req.getBookingIds());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}

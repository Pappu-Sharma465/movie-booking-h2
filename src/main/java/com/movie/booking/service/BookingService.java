package com.movie.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.booking.dto.request.BookTicketsRequest;
import com.movie.booking.dto.request.BulkBookRequest;
import com.movie.booking.dto.response.BulkCancelResponse;
import com.movie.booking.entity.Booking;
import com.movie.booking.entity.BookingStatus;
import com.movie.booking.entity.Seat;
import com.movie.booking.entity.SeatStatus;
import com.movie.booking.entity.Show;
import com.movie.booking.exception.BookingException;
import com.movie.booking.exception.BookingNotFoundException;
import com.movie.booking.exception.SeatNotAvailableException;
import com.movie.booking.exception.ShowNotFoundException;
import com.movie.booking.repository.BookingRepository;
import com.movie.booking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BookingService — B2C write operations backed by H2 via JPA.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final ShowRepository showRepo;
    private final OfferService offerService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Booking book(String showId, BookTicketsRequest req) {
        Show show = showRepo.findById(showId)
                            .orElseThrow(() -> new ShowNotFoundException(showId));

        List<String> seatIds = req.getSeatIds();

        // Fail-fast validation — check all seats before touching any
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            if (seat == null) {
                throw new BookingException("Seat " + seatId + " does not exist in show " + showId);
            }
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException(seatId);
            }
        }

        // Calculate pricing
        OfferService.PricingResult pricing =
                offerService.calculate(show.getBasePrice(), seatIds.size(), show.getStartTime());

        // Mark seats as BOOKED
        seatIds.forEach(id -> show.setSeatStatus(id, SeatStatus.BOOKED));
        showRepo.save(show);

        // Serialise discount breakdown to JSON for storage
        String breakdownJson = serialiseBreakdown(pricing.breakdown());

        // Persist booking
        String bookingId = "BKG-" + uuid();
        Booking booking = new Booking(
            bookingId,
            showId,
            show.getTheatreId(),
            req.getUserId(),
            seatIds,
            pricing.perSeatPrices(),
            pricing.total(),
            breakdownJson
        );

        return bookingRepo.save(booking);
    }

    @Transactional
    public Booking cancel(String bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                                     .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking " + bookingId + " is already cancelled.");
        }

        // Release seats
        showRepo.findById(booking.getShowId()).ifPresent(show -> {
            booking.getSeats().forEach(seatId -> {
                if (show.getSeatStatus(seatId) == SeatStatus.BOOKED) {
                    show.setSeatStatus(seatId, SeatStatus.AVAILABLE);
                }
            });
            showRepo.save(show);
        });

        booking.cancel();
        return bookingRepo.save(booking);
    }

    @Transactional(readOnly = true)
    public Booking getBooking(String bookingId) {
        return bookingRepo.findById(bookingId)
                          .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> listUserBookings(String userId) {
        return bookingRepo.findByUserIdOrderByCreatedAtAsc(userId);
    }

    private String serialiseBreakdown(List<OfferService.TicketPricing> breakdown) {
        try {
            return objectMapper.writeValueAsString(breakdown);
        } catch (Exception e) {
            log.warn("Failed to serialise discount breakdown: {}", e.getMessage());
            return "[]";
        }
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Transactional
    public List<Booking> bulkBook(BulkBookRequest req) {
        List<Booking> results = new ArrayList<>();
        for (BulkBookRequest.BookingItem item : req.getBookings()) {
            BookTicketsRequest r = new BookTicketsRequest();
            r.setUserId(item.getUserId());
            r.setSeatIds(item.getSeatIds());
            results.add(book(item.getShowId(), r));
        }
        return results;
    }

    public BulkCancelResponse bulkCancel(List<String> bookingIds) {
        List<BulkCancelResponse.CancelResult> results = new ArrayList<>();
        int succeeded = 0, failed = 0;

        for (String id : bookingIds) {
            BulkCancelResponse.CancelResult result = new BulkCancelResponse.CancelResult();
            result.setBookingId(id);
            try {
                Booking b = cancel(id);
                result.setSuccess(true);
                result.setRefundAmount(b.getTotalAmount());
                succeeded++;
            } catch (Exception ex) {
                result.setSuccess(false);
                result.setErrorMessage(ex.getMessage());
                failed++;
            }
            results.add(result);
        }

        BulkCancelResponse resp = new BulkCancelResponse();
        resp.setTotalRequested(bookingIds.size());
        resp.setSucceeded(succeeded);
        resp.setFailed(failed);
        resp.setResults(results);
        return resp;
    }

}

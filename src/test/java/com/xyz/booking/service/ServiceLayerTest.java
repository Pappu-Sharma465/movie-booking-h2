package com.movie.booking.service;

import com.movie.booking.dto.request.BookTicketsRequest;
import com.movie.booking.dto.request.CreateShowRequest;
import com.movie.booking.dto.request.OnboardTheatreRequest;
import com.movie.booking.entity.Booking;
import com.movie.booking.entity.BookingStatus;
import com.movie.booking.entity.SeatStatus;
import com.movie.booking.entity.Show;
import com.movie.booking.entity.Theatre;
import com.movie.booking.exception.BookingException;
import com.movie.booking.exception.SeatNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that boot the full Spring context with H2.
 * @DirtiesContext resets the H2 database between test classes.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ServiceLayerTest {

    @Autowired TheatreService  theatreService;
    @Autowired ShowService     showService;
    @Autowired BookingService  bookingService;
    @Autowired OfferService    offerService;

    private String theatreId, screenId, showIdAfternoon, showIdEvening;

    @BeforeEach
    void setUp() {
        OnboardTheatreRequest.ScreenRequest sr = new OnboardTheatreRequest.ScreenRequest();
        sr.setName("Test Screen"); sr.setRows(8); sr.setSeatsPerRow(10);

        OnboardTheatreRequest tr = new OnboardTheatreRequest();
        tr.setName("Test Theatre"); tr.setCity("TestCity"); tr.setAddress("1 Test St");
        tr.setScreens(List.of(sr));

        Theatre theatre = theatreService.onboard(tr);
        theatreId = theatre.getId();
        screenId  = theatre.getScreens().get(0).getId();

        showIdAfternoon = showService.createShow(
            buildShow(theatreId, screenId, "TestMovie", "English", "Drama",
                      "2025-09-15", "14:00", "17:00", 200)).getId();

        showIdEvening = showService.createShow(
            buildShow(theatreId, screenId, "TestMovie", "English", "Drama",
                      "2025-09-15", "20:00", "23:00", 200)).getId();
    }

    // =========================================================================
    // OfferService
    // =========================================================================

    @Nested @DisplayName("OfferService")
    class OfferServiceTests {

        @Test @DisplayName("No discount: 1 ticket evening show")
        void noDiscount() {
            var r = offerService.calculate(200, 1, "20:00");
            assertEquals(200.0, r.total(), 0.01);
            assertTrue(r.breakdown().get(0).discountReasons().isEmpty());
        }

        @Test @DisplayName("20% off all tickets for afternoon show")
        void afternoonDiscount() {
            var r = offerService.calculate(200, 2, "14:00");
            assertEquals(320.0, r.total(), 0.01);    // 2 × 160
        }

        @Test @DisplayName("50% off every 3rd ticket")
        void thirdTicketDiscount() {
            var r = offerService.calculate(200, 3, "20:00");
            assertEquals(500.0, r.total(), 0.01);    // 200 + 200 + 100
        }

        @Test @DisplayName("Both discounts stack on 3rd ticket of afternoon show")
        void stackedDiscounts() {
            var r = offerService.calculate(200, 3, "14:00");
            // t1=160, t2=160, t3=200-100-40=60
            assertEquals(380.0, r.total(), 0.01);
            assertEquals(2, r.breakdown().get(2).discountReasons().size());
        }

        @Test @DisplayName("Afternoon boundary: 12:00 in, 17:00 out")
        void afternoonBoundaries() {
            assertTrue(offerService.isAfternoonShow("12:00"));
            assertTrue(offerService.isAfternoonShow("16:59"));
            assertFalse(offerService.isAfternoonShow("17:00"));
            assertFalse(offerService.isAfternoonShow("11:59"));
        }
    }

    // =========================================================================
    // TheatreService
    // =========================================================================

    @Nested @DisplayName("TheatreService")
    class TheatreServiceTests {

        @Test @DisplayName("Seat grid is auto-generated on show creation")
        void autoSeatInventory() {
            Show show = showService.getShow(showIdAfternoon);
            assertEquals(80, show.getSeats().size());  // 8 rows × 10 seats
            assertTrue(show.getSeats().stream().allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE));
        }

        @Test @DisplayName("allocateSeatInventory replaces grid when no seats booked")
        void reallocate() {
            int total = theatreService.allocateSeatInventory(showIdAfternoon, 5, 6);
            assertEquals(30, total);
            assertEquals(30, showService.getShow(showIdAfternoon).getSeats().size());
        }

        @Test @DisplayName("allocateSeatInventory blocked when booked seats exist")
        void reallocateBlockedWhenBooked() {
            book(showIdAfternoon, "user-1", List.of("A1"));
            assertThrows(BookingException.class,
                () -> theatreService.allocateSeatInventory(showIdAfternoon, 5, 6));
        }

        @Test @DisplayName("updateSeatStatus blocks and unblocks seats")
        void blockUnblock() {
            theatreService.updateSeatStatus(showIdAfternoon, List.of("A1", "A2"), SeatStatus.BLOCKED);
            assertEquals(SeatStatus.BLOCKED, showService.getShow(showIdAfternoon).getSeatStatus("A1"));

            theatreService.updateSeatStatus(showIdAfternoon, List.of("A1"), SeatStatus.AVAILABLE);
            assertEquals(SeatStatus.AVAILABLE, showService.getShow(showIdAfternoon).getSeatStatus("A1"));
        }

        @Test @DisplayName("updateSeatStatus cannot set BOOKED directly")
        void cannotSetBooked() {
            assertThrows(BookingException.class,
                () -> theatreService.updateSeatStatus(showIdAfternoon, List.of("A1"), SeatStatus.BOOKED));
        }
    }

    // =========================================================================
    // ShowService
    // =========================================================================

    @Nested @DisplayName("ShowService")
    class ShowServiceTests {

        @Test @DisplayName("browseShows returns shows matching city and date")
        void browseShows() {
            var results = showService.browseShows("TestMovie", "TestCity", "2025-09-15", null, null);
            assertEquals(2, results.size());
        }

        @Test @DisplayName("browseShows language filter works")
        void browseByLanguage() {
            showService.createShow(buildShow(theatreId, screenId,
                "TestMovie", "Hindi", "Drama", "2025-09-15", "18:00", "21:00", 200));
            var results = showService.browseShows("TestMovie", "TestCity", "2025-09-15", "Hindi", null);
            assertEquals(1, results.size());
        }

        @Test @DisplayName("browseShows city is case-insensitive")
        void browseCaseInsensitive() {
            var results = showService.browseShows("testmovie", "TESTCITY", "2025-09-15", null, null);
            assertEquals(2, results.size());
        }

        @Test @DisplayName("deleteShow blocked when confirmed bookings exist")
        void deleteBlockedByBookings() {
            book(showIdAfternoon, "user-1", List.of("A1"));
            assertThrows(BookingException.class, () -> showService.deleteShow(showIdAfternoon));
        }

        @Test @DisplayName("deleteShow succeeds with no confirmed bookings")
        void deleteSucceeds() {
            assertDoesNotThrow(() -> showService.deleteShow(showIdEvening));
        }

        @Test @DisplayName("getSeatMap groups seats by row correctly")
        void seatMapGroupedByRow() {
            var map = showService.getSeatMap(showIdAfternoon);
            assertTrue(map.getRows().containsKey("A"));
            assertEquals(10, map.getRows().get("A").size());
        }
    }

    // =========================================================================
    // BookingService
    // =========================================================================

    @Nested @DisplayName("BookingService")
    class BookingServiceTests {

        @Test @DisplayName("Booking marks seats as BOOKED and persists")
        void bookingMarksSeatBooked() {
            Booking b = book(showIdAfternoon, "user-1", List.of("A1", "A2", "A3"));
            assertEquals(BookingStatus.CONFIRMED, b.getStatus());

            Show show = showService.getShow(showIdAfternoon);
            assertEquals(SeatStatus.BOOKED, show.getSeatStatus("A1"));
        }

        @Test @DisplayName("Booking fails for already-booked seat")
        void bookingFailsForTakenSeat() {
            book(showIdAfternoon, "user-1", List.of("A1"));
            assertThrows(SeatNotAvailableException.class,
                () -> book(showIdAfternoon, "user-2", List.of("A1")));
        }

        @Test @DisplayName("Booking fails for blocked seat")
        void bookingFailsForBlockedSeat() {
            theatreService.updateSeatStatus(showIdAfternoon, List.of("B1"), SeatStatus.BLOCKED);
            assertThrows(SeatNotAvailableException.class,
                () -> book(showIdAfternoon, "user-1", List.of("B1")));
        }

        @Test @DisplayName("Afternoon discount applied correctly")
        void afternoonDiscountInBooking() {
            Booking b = book(showIdAfternoon, "user-1", List.of("A1", "A2", "A3"));
            // base=200: t1=160, t2=160, t3=200-100-40=60 → total=380
            assertEquals(380.0, b.getTotalAmount(), 0.01);
        }

        @Test @DisplayName("Cancel frees seats back to AVAILABLE")
        void cancelFreesSeats() {
            Booking b = book(showIdAfternoon, "user-1", List.of("C1", "C2"));
            bookingService.cancel(b.getId());

            Show show = showService.getShow(showIdAfternoon);
            assertEquals(SeatStatus.AVAILABLE, show.getSeatStatus("C1"));
            assertEquals(SeatStatus.AVAILABLE, show.getSeatStatus("C2"));
        }

        @Test @DisplayName("Cancel throws when already cancelled")
        void cancelAlreadyCancelled() {
            Booking b = book(showIdAfternoon, "user-1", List.of("D1"));
            bookingService.cancel(b.getId());
            assertThrows(BookingException.class, () -> bookingService.cancel(b.getId()));
        }

        @Test @DisplayName("bulkBook rolls back entirely on failure")
        void bulkBookRollback() {
            // Pre-book A1 on the evening show to force failure
            book(showIdEvening, "user-x", List.of("A1"));

            BulkBookRequest req = new BulkBookRequest();
            BulkBookRequest.BookingItem item1 = new BulkBookRequest.BookingItem();
            item1.setShowId(showIdAfternoon); item1.setUserId("user-1");
            item1.setSeatIds(List.of("E1", "E2"));

            BulkBookRequest.BookingItem item2 = new BulkBookRequest.BookingItem();
            item2.setShowId(showIdEvening); item2.setUserId("user-1");
            item2.setSeatIds(List.of("A1")); // already booked — will fail

            req.setBookings(List.of(item1, item2));

            assertThrows(Exception.class, () -> bookingService.bulkBook(req));

            // E1 and E2 must have been rolled back
            Show afternoonShow = showService.getShow(showIdAfternoon);
            assertEquals(SeatStatus.AVAILABLE, afternoonShow.getSeatStatus("E1"));
            assertEquals(SeatStatus.AVAILABLE, afternoonShow.getSeatStatus("E2"));
        }

        @Test @DisplayName("bulkCancel handles partial failures independently")
        void bulkCancelPartial() {
            Booking b1 = book(showIdAfternoon, "user-1", List.of("F1"));
            Booking b2 = book(showIdAfternoon, "user-1", List.of("F2"));
            bookingService.cancel(b2.getId());

            var result = bookingService.bulkCancel(List.of(b1.getId(), b2.getId(), "BKG-INVALID"));
            assertEquals(3, result.getTotalRequested());
            assertEquals(1, result.getSucceeded());
            assertEquals(2, result.getFailed());
        }

        @Test @DisplayName("listUserBookings returns correct results")
        void listUserBookings() {
            book(showIdAfternoon, "user-A", List.of("G1"));
            book(showIdAfternoon, "user-A", List.of("G2"));
            book(showIdAfternoon, "user-B", List.of("G3"));

            assertEquals(2, bookingService.listUserBookings("user-A").size());
            assertEquals(1, bookingService.listUserBookings("user-B").size());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Booking book(String showId, String userId, List<String> seatIds) {
        BookTicketsRequest r = new BookTicketsRequest();
        r.setUserId(userId);
        r.setSeatIds(seatIds);
        return bookingService.book(showId, r);
    }

    private static CreateShowRequest buildShow(
            String theatreId, String screenId, String movie, String lang, String genre,
            String date, String start, String end, double price) {
        CreateShowRequest r = new CreateShowRequest();
        r.setTheatreId(theatreId); r.setScreenId(screenId);
        r.setMovieTitle(movie);    r.setLanguage(lang);    r.setGenre(genre);
        r.setDate(date);           r.setStartTime(start);  r.setEndTime(end);
        r.setBasePrice(price);
        return r;
    }
}

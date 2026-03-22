package com.movie.booking.service;

import com.movie.booking.dto.request.OnboardTheatreRequest;
import com.movie.booking.entity.Screen;
import com.movie.booking.entity.Seat;
import com.movie.booking.entity.SeatStatus;
import com.movie.booking.entity.Show;
import com.movie.booking.entity.Theatre;
import com.movie.booking.exception.BookingException;
import com.movie.booking.exception.ShowNotFoundException;
import com.movie.booking.exception.TheatreNotFoundException;
import com.movie.booking.repository.SeatRepository;
import com.movie.booking.repository.ShowRepository;
import com.movie.booking.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TheatreService — B2B partner operations.
 *
 *  - onboard()               Register a theatre with screens
 *  - allocateSeatInventory() Initialise or resize a show's seat grid
 *  - updateSeatStatus()      Block / unblock individual seats
 */
@Service
@RequiredArgsConstructor
public class TheatreService {

    private final TheatreRepository theatreRepo;
    private final ShowRepository    showRepo;
    private final SeatRepository    seatRepo;

    @Transactional
    public Theatre onboard(OnboardTheatreRequest req) {
        Theatre theatre = new Theatre(
            "THR-" + uuid(),
            req.getName(),
            req.getCity(),
            req.getAddress()
        );

        if (req.getScreens() != null) {
            for (OnboardTheatreRequest.ScreenRequest sr : req.getScreens()) {
                Screen screen = new Screen("SCR-" + uuid(), sr.getName(), sr.getRows(), sr.getSeatsPerRow());
                theatre.addScreen(screen);
            }
        }

        return theatreRepo.save(theatre);
    }

    @Transactional
    public int allocateSeatInventory(String showId, int rows, int seatsPerRow) {
        Show show = requireShow(showId);

        // Block reallocation if booked seats exist
        long bookedCount = seatRepo.countByShowIdAndStatus(showId, SeatStatus.BOOKED);
        if (bookedCount > 0) {
            throw new BookingException(
                "Cannot reallocate seats for show " + showId + ": "
                + bookedCount + " seat(s) are already BOOKED.");
        }

        // Remove existing seats
        seatRepo.deleteAllByShowId(showId);
        show.clearSeats();

        // Create new seat grid
        List<String> seatIds = generateSeatIds(rows, seatsPerRow);
        seatIds.forEach(id -> show.addSeat(new Seat(id)));

        showRepo.save(show);
        return seatIds.size();
    }

    @Transactional
    public void updateSeatStatus(String showId, List<String> seatIds, SeatStatus status) {
        if (status == SeatStatus.BOOKED) {
            throw new BookingException("Seats may only be set to AVAILABLE or BLOCKED via this method.");
        }

        Show show = requireShow(showId);

        for (String seatId : seatIds) {
            if (!show.hasSeat(seatId)) {
                throw new BookingException("Seat " + seatId + " not found in show " + showId);
            }
            if (show.getSeatStatus(seatId) == SeatStatus.BOOKED) {
                throw new BookingException(
                    "Seat " + seatId + " is BOOKED — cancel the booking before changing its status.");
            }
            show.setSeatStatus(seatId, status);
        }

        showRepo.save(show);
    }

    public Theatre getTheatre(String theatreId) {
        return theatreRepo.findById(theatreId)
                          .orElseThrow(() -> new TheatreNotFoundException(theatreId));
    }

    public List<Theatre> getAllTheatres() {
        return theatreRepo.findAll();
    }

    public static List<String> generateSeatIds(int rows, int seatsPerRow) {
        List<String> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            char rowLabel = (char) ('A' + r);
            for (int c = 1; c <= seatsPerRow; c++) {
                seats.add(rowLabel + String.valueOf(c));
            }
        }
        return seats;
    }

    Show requireShow(String showId) {
        return showRepo.findById(showId)
                       .orElseThrow(() -> new ShowNotFoundException(showId));
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

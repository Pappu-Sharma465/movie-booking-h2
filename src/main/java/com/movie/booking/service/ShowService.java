package com.movie.booking.service;

import com.movie.booking.dto.request.CreateShowRequest;
import com.movie.booking.dto.request.UpdateShowRequest;
import com.movie.booking.dto.response.SeatMapResponse;
import com.movie.booking.dto.response.ShowSummaryResponse;
import com.movie.booking.entity.Screen;
import com.movie.booking.entity.Seat;
import com.movie.booking.entity.Show;
import com.movie.booking.entity.Theatre;
import com.movie.booking.exception.BookingException;
import com.movie.booking.exception.ShowNotFoundException;
import com.movie.booking.exception.TheatreNotFoundException;
import com.movie.booking.repository.ShowRepository;
import com.movie.booking.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ShowService — manages the full show lifecycle (B2B) and provides
 * browsing and seat-map views to end customers (B2C).
 */
@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository     showRepo;
    private final TheatreRepository  theatreRepo;
    private final OfferService       offerService;


    @Transactional
    public Show createShow(CreateShowRequest req) {
        Theatre theatre = theatreRepo.findById(req.getTheatreId())
                                     .orElseThrow(() -> new TheatreNotFoundException(req.getTheatreId()));

        Screen screen = theatre.getScreens().stream()
                               .filter(s -> s.getId().equals(req.getScreenId()))
                               .findFirst()
                               .orElseThrow(() -> new BookingException(
                                   "Screen " + req.getScreenId() + " not found in theatre " + req.getTheatreId()));

        String showId = "SHW-" + uuid();
        Show show = new Show(
            showId,
            req.getTheatreId(),
            req.getScreenId(),
            req.getMovieTitle(),
            req.getLanguage(),
            req.getGenre(),
            req.getDate(),
            req.getStartTime(),
            req.getEndTime(),
            req.getBasePrice()
        );

        // Auto-generate seat grid from screen dimensions
        TheatreService.generateSeatIds(screen.getRows(), screen.getSeatsPerRow())
                      .forEach(id -> show.addSeat(new Seat(id)));

        return showRepo.save(show);
    }

    @Transactional
    public Show updateShow(String showId, UpdateShowRequest req) {
        Show show = requireShow(showId);

        if (req.getMovieTitle() != null) show.setMovieTitle(req.getMovieTitle());
        if (req.getLanguage()   != null) show.setLanguage(req.getLanguage());
        if (req.getGenre()      != null) show.setGenre(req.getGenre());
        if (req.getDate()       != null) show.setDate(req.getDate());
        if (req.getStartTime()  != null) show.setStartTime(req.getStartTime());
        if (req.getEndTime()    != null) show.setEndTime(req.getEndTime());
        if (req.getBasePrice()  != null) {
            if (req.getBasePrice() <= 0) throw new BookingException("basePrice must be > 0");
            show.setBasePrice(req.getBasePrice());
        }

        return showRepo.save(show);
    }

    @Transactional
    public void deleteShow(String showId) {
        requireShow(showId);  // existence check

        if (showRepo.hasConfirmedBookings(showId)) {
            throw new BookingException(
                "Cannot delete show " + showId + ": confirmed bookings exist. Cancel them first.");
        }

        showRepo.deleteById(showId);
    }


    @Transactional(readOnly = true)
    public List<ShowSummaryResponse> browseShows(String movie, String city, String date,
                                                  String language, String genre) {
        String langParam  = (language != null && !language.isBlank()) ? language : null;
        String genreParam = (genre    != null && !genre.isBlank())    ? genre    : null;

        List<Show> shows = showRepo.browseShows(movie, city, date, langParam, genreParam);

        return shows.stream().map(show -> {
            Theatre theatre = theatreRepo.findById(show.getTheatreId()).orElse(null);
            return ShowSummaryResponse.from(show, theatre);
        }).toList();
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(String showId) {
        Show show = requireShow(showId);

        Map<String, List<SeatMapResponse.SeatDetail>> rows = new TreeMap<>();
        for (Seat seat : show.getSeats()) {
            String rowKey = String.valueOf(seat.getSeatId().charAt(0));
            rows.computeIfAbsent(rowKey, k -> new ArrayList<>())
                .add(new SeatMapResponse.SeatDetail(seat.getSeatId(), seat.getStatus()));
        }

        SeatMapResponse resp = new SeatMapResponse();
        resp.setShowId(show.getId());
        resp.setMovieTitle(show.getMovieTitle());
        resp.setDate(show.getDate());
        resp.setStartTime(show.getStartTime());
        resp.setBasePrice(show.getBasePrice());
        resp.setTotalSeats(show.getSeats().size());
        resp.setAvailableSeats(show.countAvailable());
        resp.setRows(rows);
        return resp;
    }

    @Transactional(readOnly = true)
    public Show getShow(String showId) {
        return requireShow(showId);
    }

    Show requireShow(String showId) {
        return showRepo.findById(showId)
                       .orElseThrow(() -> new ShowNotFoundException(showId));
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}

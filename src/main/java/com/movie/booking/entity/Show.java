package com.movie.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "seats")
public class Show {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "theatre_id", nullable = false)
    private String theatreId;

    @Column(name = "screen_id", nullable = false)
    private String screenId;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(nullable = false)
    private String language;    // lower-case

    @Column(nullable = false)
    private String genre;       // lower-case

    @Column(nullable = false)
    private String date;        // "YYYY-MM-DD"

    @Column(name = "start_time", nullable = false)
    private String startTime;   // "HH:MM"

    @Column(name = "end_time", nullable = false)
    private String endTime;     // "HH:MM"

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Seat> seats = new ArrayList<>();

    public Show(String id, String theatreId, String screenId,
                String movieTitle, String language, String genre,
                String date, String startTime, String endTime, double basePrice) {
        this.id         = id;
        this.theatreId  = theatreId;
        this.screenId   = screenId;
        this.movieTitle = movieTitle;
        this.language   = language.toLowerCase();
        this.genre      = genre.toLowerCase();
        this.date       = date;
        this.startTime  = startTime;
        this.endTime    = endTime;
        this.basePrice  = basePrice;
    }

    public void setLanguage(String language) { this.language = language.toLowerCase(); }
    public void setGenre(String genre)       { this.genre    = genre.toLowerCase(); }


    public void addSeat(Seat seat) {
        seat.setShow(this);
        seats.add(seat);
    }

    public void clearSeats() {
        seats.forEach(s -> s.setShow(null));
        seats.clear();
    }

    public Seat findSeat(String seatId) {
        return seats.stream().filter(s -> s.getSeatId().equals(seatId)).findFirst().orElse(null);
    }

    public boolean hasSeat(String seatId) {
        return findSeat(seatId) != null;
    }

    public SeatStatus getSeatStatus(String seatId) {
        Seat seat = findSeat(seatId);
        return seat != null ? seat.getStatus() : null;
    }

    public void setSeatStatus(String seatId, SeatStatus status) {
        Seat seat = findSeat(seatId);
        if (seat != null) seat.setStatus(status);
    }

    public long countAvailable() {
        return seats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE).count();
    }
}

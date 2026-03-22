package com.movie.booking.dto.response;

import com.movie.booking.entity.Show;
import com.movie.booking.entity.Theatre;
import lombok.Data;

@Data
public class ShowSummaryResponse {

    private String  showId;
    private String  theatreName;
    private String  city;
    private String  movieTitle;
    private String  language;
    private String  genre;
    private String  date;
    private String  startTime;
    private String  endTime;
    private double  basePrice;
    private long    availableSeats;
    private long    totalSeats;
    private boolean afternoonShow;

    public static ShowSummaryResponse from(Show show, Theatre theatre) {
        ShowSummaryResponse r = new ShowSummaryResponse();
        r.setShowId(show.getId());
        r.setTheatreName(theatre.getName());
        r.setCity(theatre.getCity());
        r.setMovieTitle(show.getMovieTitle());
        r.setLanguage(show.getLanguage());
        r.setGenre(show.getGenre());
        r.setDate(show.getDate());
        r.setStartTime(show.getStartTime());
        r.setEndTime(show.getEndTime());
        r.setBasePrice(show.getBasePrice());
        r.setAvailableSeats(show.countAvailable());
        r.setTotalSeats(show.getSeats().size());
        r.setAfternoonShow(isAfternoon(show.getStartTime()));
        return r;
    }

    private static boolean isAfternoon(String startTime) {
        String[] parts = startTime.split(":");
        int mins = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        return mins >= 720 && mins < 1020;
    }
}

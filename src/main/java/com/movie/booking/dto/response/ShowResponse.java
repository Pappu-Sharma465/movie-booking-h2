package com.movie.booking.dto.response;

import com.movie.booking.entity.Show;
import lombok.Data;

@Data
public class ShowResponse {

    private String  id;
    private String  theatreId;
    private String  screenId;
    private String  movieTitle;
    private String  language;
    private String  genre;
    private String  date;
    private String  startTime;
    private String  endTime;
    private double  basePrice;
    private long    totalSeats;
    private long    availableSeats;
    private boolean afternoonShow;

    public static ShowResponse from(Show show) {
        ShowResponse r = new ShowResponse();
        r.setId(show.getId());
        r.setTheatreId(show.getTheatreId());
        r.setScreenId(show.getScreenId());
        r.setMovieTitle(show.getMovieTitle());
        r.setLanguage(show.getLanguage());
        r.setGenre(show.getGenre());
        r.setDate(show.getDate());
        r.setStartTime(show.getStartTime());
        r.setEndTime(show.getEndTime());
        r.setBasePrice(show.getBasePrice());
        r.setTotalSeats(show.getSeats().size());
        r.setAvailableSeats(show.countAvailable());
        r.setAfternoonShow(isAfternoon(show.getStartTime()));
        return r;
    }

    private static boolean isAfternoon(String startTime) {
        String[] parts = startTime.split(":");
        int mins = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        return mins >= 720 && mins < 1020;
    }
}

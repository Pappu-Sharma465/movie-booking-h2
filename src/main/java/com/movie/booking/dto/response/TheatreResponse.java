package com.movie.booking.dto.response;

import com.movie.booking.entity.Theatre;
import lombok.Data;

import java.util.List;

@Data
public class TheatreResponse {

    private String id;
    private String name;
    private String city;
    private String address;
    private List<ScreenResponse> screens;

    @Data
    public static class ScreenResponse {
        private String id;
        private String name;
        private int    rows;
        private int    seatsPerRow;
        private int    totalSeats;
    }

    public static TheatreResponse from(Theatre theatre) {
        TheatreResponse r = new TheatreResponse();
        r.setId(theatre.getId());
        r.setName(theatre.getName());
        r.setCity(theatre.getCity());
        r.setAddress(theatre.getAddress());
        r.setScreens(theatre.getScreens().stream().map(s -> {
            ScreenResponse sr = new ScreenResponse();
            sr.setId(s.getId());
            sr.setName(s.getName());
            sr.setRows(s.getRows());
            sr.setSeatsPerRow(s.getSeatsPerRow());
            sr.setTotalSeats(s.getRows() * s.getSeatsPerRow());
            return sr;
        }).toList());
        return r;
    }
}

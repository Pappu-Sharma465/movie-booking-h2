package com.movie.booking.config;

import com.movie.booking.dto.request.CreateShowRequest;
import com.movie.booking.dto.request.OnboardTheatreRequest;
import com.movie.booking.entity.Theatre;
import com.movie.booking.service.ShowService;
import com.movie.booking.service.TheatreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final TheatreService theatreService;
    private final ShowService    showService;

    @Bean
    public CommandLineRunner seedDemoData() {
        return args -> {

            OnboardTheatreRequest pvrReq = buildTheatreRequest(
                "PVR Cinemas Koramangala", "Bangalore", "5th Block, Koramangala, Bangalore",
                List.of(
                    buildScreen("Screen 1 Gold",    10, 12),
                    buildScreen("Screen 2 Classic",  8, 10)
                )
            );
            Theatre pvr = theatreService.onboard(pvrReq);
            OnboardTheatreRequest inoxReq = buildTheatreRequest(
                "INOX Garuda Mall", "Bangalore", "1 Magrath Road, Ashok Nagar, Bangalore",
                List.of(buildScreen("Audi 1 Insignia", 12, 15))
            );
            Theatre inox = theatreService.onboard(inoxReq);

            String scrPvr1  = pvr.getScreens().get(0).getId();
            String scrPvr2  = pvr.getScreens().get(1).getId();
            String scrInox1 = inox.getScreens().get(0).getId();

            showService.createShow(buildShow(
                pvr.getId(), scrPvr1,
                "Dune: Part Three", "English", "Sci-Fi",
                "2025-09-15", "14:00", "17:15", 350
            ));
            showService.createShow(buildShow(
                pvr.getId(), scrPvr2,
                "Dhurandhar 2", "Hindi", "Adventure",
                "2025-09-15", "19:30", "22:45", 250
            ));
            showService.createShow(buildShow(
                inox.getId(), scrInox1,
                "3 Idiots", "Hindi", "Comedy",
                "2025-09-15", "15:30", "18:45", 400
            ));
           };
    }

    private static OnboardTheatreRequest buildTheatreRequest(
            String name, String city, String address,
            List<OnboardTheatreRequest.ScreenRequest> screens) {
        OnboardTheatreRequest r = new OnboardTheatreRequest();
        r.setName(name); r.setCity(city); r.setAddress(address);
        r.setScreens(screens);
        return r;
    }

    private static OnboardTheatreRequest.ScreenRequest buildScreen(String name, int rows, int seatsPerRow) {
        OnboardTheatreRequest.ScreenRequest sr = new OnboardTheatreRequest.ScreenRequest();
        sr.setName(name); sr.setRows(rows); sr.setSeatsPerRow(seatsPerRow);
        return sr;
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

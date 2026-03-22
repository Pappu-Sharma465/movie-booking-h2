package com.movie.booking.repository;

import com.movie.booking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

    /**
     * Browse shows: find all shows for a movie (partial match) in a city on a date.
     * Joins through Theatre to filter by city.
     */
    @Query("""
        SELECT s FROM Show s
        JOIN Theatre t ON t.id = s.theatreId
        WHERE LOWER(t.city)       = LOWER(:city)
          AND LOWER(s.movieTitle) LIKE LOWER(CONCAT('%', :movie, '%'))
          AND s.date              = :date
          AND (:language IS NULL  OR LOWER(s.language) = LOWER(:language))
          AND (:genre    IS NULL  OR LOWER(s.genre)    = LOWER(:genre))
        ORDER BY s.startTime ASC
        """)
    List<Show> browseShows(
        @Param("movie")    String movie,
        @Param("city")     String city,
        @Param("date")     String date,
        @Param("language") String language,
        @Param("genre")    String genre
    );

    /**
     * Check whether any confirmed booking exists for a show (used before deletion).
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.showId = :showId
          AND b.status = com.movie.booking.entity.BookingStatus.CONFIRMED
        """)
    boolean hasConfirmedBookings(@Param("showId") String showId);

    List<Show> findByTheatreId(String theatreId);
}

package com.movie.booking.repository;

import com.movie.booking.entity.Seat;
import com.movie.booking.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /** Count BOOKED seats for a show — used before reallocating inventory. */
    long countByShowIdAndStatus(String showId, SeatStatus status);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.show.id = :showId")
    void deleteAllByShowId(@Param("showId") String showId);
}

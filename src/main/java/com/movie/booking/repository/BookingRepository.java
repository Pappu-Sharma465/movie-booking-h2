package com.movie.booking.repository;

import com.movie.booking.entity.Booking;
import com.movie.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByUserIdOrderByCreatedAtAsc(String userId);

    boolean existsByShowIdAndStatus(String showId, BookingStatus status);
}

package com.khanabook.pos.repository;

import com.khanabook.pos.model.BookingStatus;
import com.khanabook.pos.model.TableBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Long> {
    Page<TableBooking> findByStatus(BookingStatus status, Pageable pageable);
    List<TableBooking> findByBookingDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<TableBooking> findByRestaurantTableIdAndBookingDateTimeBetween(
        Long tableId, LocalDateTime start, LocalDateTime end);
}

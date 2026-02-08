package com.khanabook.pos.service;

import com.khanabook.pos.dto.request.BookingRequest;
import com.khanabook.pos.model.TableBooking;
import com.khanabook.pos.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {

	TableBooking createBooking(BookingRequest bookingRequest);

	Optional<TableBooking> getBookingById(Long id);

	Page<TableBooking> getAllBookings(Pageable pageable);

	Page<TableBooking> getBookingsByStatus(BookingStatus status, Pageable pageable);

	List<TableBooking> getBookingsBetween(LocalDateTime start, LocalDateTime end);

	TableBooking updateBookingStatus(Long id, BookingStatus newStatus);

	TableBooking cancelBooking(Long id);

	TableBooking confirmBooking(Long id);
}

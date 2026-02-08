package com.khanabook.pos.service.impl;

import com.khanabook.pos.dto.request.BookingRequest;
import com.khanabook.pos.exception.InvalidOrderStateException;
import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.TableBooking;
import com.khanabook.pos.model.BookingStatus;
import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;
import com.khanabook.pos.repository.RestaurantTableRepository;
import com.khanabook.pos.repository.TableBookingRepository;
import com.khanabook.pos.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TableBookingRepository tableBookingRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    @Override @Transactional
    public TableBooking createBooking(BookingRequest bookingRequest) {
        RestaurantTable table = restaurantTableRepository.findById(bookingRequest.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant Table not found with id: " + bookingRequest.getTableId()));

        // Basic check for overlapping bookings - more complex logic might be needed
        List<TableBooking> existingBookings = tableBookingRepository.findByRestaurantTableIdAndBookingDateTimeBetween(
                table.getId(), bookingRequest.getBookingDateTime().minusHours(2), bookingRequest.getBookingDateTime().plusHours(2));
        
        // This simple check prevents direct overlaps, but real systems would need to handle booking durations
        if (!existingBookings.isEmpty()) {
            throw new InvalidOrderStateException("Table already booked for a nearby time slot.");
        }

        TableBooking booking = TableBooking.builder()
                .restaurantTable(table)
                .customerName(bookingRequest.getCustomerName())
                .customerPhone(bookingRequest.getCustomerPhone())
                .customerEmail(bookingRequest.getCustomerEmail())
                .partySize(bookingRequest.getPartySize())
                .bookingDateTime(bookingRequest.getBookingDateTime())
                .status(BookingStatus.PENDING) // New bookings are pending by default
                .specialRequests(bookingRequest.getSpecialRequests())
                .build();
        
        // Optionally update table status to RESERVED if confirmed immediately
        // For now, new bookings are PENDING, table status remains AVAILABLE until CONFIRMED.

        return tableBookingRepository.save(booking);
    }

    @Override
    public Optional<TableBooking> getBookingById(Long id) {
        return tableBookingRepository.findById(id);
    }

    @Override
    public Page<TableBooking> getAllBookings(Pageable pageable) {
        return tableBookingRepository.findAll(pageable);
    }

    @Override
    public Page<TableBooking> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        return tableBookingRepository.findByStatus(status, pageable);
    }

    @Override
    public List<TableBooking> getBookingsBetween(LocalDateTime start, LocalDateTime end) {
        return tableBookingRepository.findByBookingDateTimeBetween(start, end);
    }

    @Override @Transactional
    public TableBooking updateBookingStatus(Long id, BookingStatus newStatus) {
        TableBooking booking = tableBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table Booking not found with id: " + id));

        // State machine for booking status
        switch (newStatus) {
            case CONFIRMED:
                if (booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(newStatus);
                    booking.setConfirmedAt(LocalDateTime.now());
                    booking.getRestaurantTable().setStatus(TableStatus.RESERVED);
                    restaurantTableRepository.save(booking.getRestaurantTable());
                } else {
                    throw new InvalidOrderStateException("Cannot confirm a booking that is not PENDING.");
                }
                break;
            case SEATED:
                if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                    booking.setStatus(newStatus);
                    // Optionally link to an order here if it's created upon seating
                    booking.getRestaurantTable().setStatus(TableStatus.OCCUPIED);
                    restaurantTableRepository.save(booking.getRestaurantTable());
                } else {
                    throw new InvalidOrderStateException("Cannot seat a booking that is not CONFIRMED or PENDING.");
                }
                break;
            case COMPLETED:
                if (booking.getStatus() == BookingStatus.SEATED) {
                    booking.setStatus(newStatus);
                    // Table should be freed up once order is completed, not necessarily by booking completion
                } else {
                    throw new InvalidOrderStateException("Cannot complete a booking that is not SEATED.");
                }
                break;
            case CANCELLED:
                if (booking.getStatus() != BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.NO_SHOW) {
                    booking.setStatus(newStatus);
                    if (booking.getRestaurantTable() != null && booking.getRestaurantTable().getStatus() == TableStatus.RESERVED) {
                        booking.getRestaurantTable().setStatus(TableStatus.AVAILABLE);
                        restaurantTableRepository.save(booking.getRestaurantTable());
                    }
                } else {
                    throw new InvalidOrderStateException("Cannot cancel a completed or no-show booking.");
                }
                break;
            case NO_SHOW:
                if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) {
                    booking.setStatus(newStatus);
                    if (booking.getRestaurantTable() != null && booking.getRestaurantTable().getStatus() == TableStatus.RESERVED) {
                        booking.getRestaurantTable().setStatus(TableStatus.AVAILABLE);
                        restaurantTableRepository.save(booking.getRestaurantTable());
                    }
                } else {
                    throw new InvalidOrderStateException("Cannot mark as NO_SHOW if already SEATED or COMPLETED.");
                }
                break;
            default:
                throw new InvalidOrderStateException("Invalid booking status transition.");
        }
        
        return tableBookingRepository.save(booking);
    }

    @Override @Transactional
    public TableBooking cancelBooking(Long id) {
        return updateBookingStatus(id, BookingStatus.CANCELLED);
    }

    @Override @Transactional
    public TableBooking confirmBooking(Long id) {
        return updateBookingStatus(id, BookingStatus.CONFIRMED);
    }
}

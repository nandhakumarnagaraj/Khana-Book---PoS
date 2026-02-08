package com.khanabook.pos.controller;

import com.khanabook.pos.dto.request.BookingRequest;
import com.khanabook.pos.model.BookingStatus;
import com.khanabook.pos.model.TableBooking;
import com.khanabook.pos.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController @RequestMapping("/api/bookings")
@RequiredArgsConstructor @Tag(name = "Bookings", description = "Table booking management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new table booking")
    public ResponseEntity<TableBooking> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(bookingRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<TableBooking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all bookings with pagination")
    public ResponseEntity<Page<TableBooking>> getAllBookings(Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/status/{status}") @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get bookings by status with pagination")
    public ResponseEntity<Page<TableBooking>> getBookingsByStatus(@PathVariable BookingStatus status, Pageable pageable) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status, pageable));
    }

    @GetMapping("/between") @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get bookings between two dates/times")
    public ResponseEntity<List<TableBooking>> getBookingsBetween(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(bookingService.getBookingsBetween(start, end));
    }

    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update booking status")
    public ResponseEntity<TableBooking> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }

    @PatchMapping("/{id}/cancel") @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<TableBooking> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PatchMapping("/{id}/confirm") @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Confirm a booking")
    public ResponseEntity<TableBooking> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }
}

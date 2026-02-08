package com.khanabook.pos.controller;

import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.model.OrderType;
import com.khanabook.pos.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/reports")
@RequiredArgsConstructor @Tag(name = "Reports", description = "Reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // Restrict all reports to Admin/Manager
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    @Operation(summary = "Get sales orders between two dates")
    public ResponseEntity<List<CustomerOrder>> getSalesBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getSalesBetweenDates(start, end));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get total revenue between two dates")
    public ResponseEntity<Double> getTotalRevenueBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getTotalRevenueBetweenDates(start, end));
    }

    @GetMapping("/order-status-count")
    @Operation(summary = "Get count of orders by status")
    public ResponseEntity<Map<String, Long>> getOrderCountByStatus() {
        return ResponseEntity.ok(reportService.getOrderCountByStatus());
    }

    @GetMapping("/order-type-count")
    @Operation(summary = "Get count of orders by type (DINE_IN, TAKEAWAY, DELIVERY)")
    public ResponseEntity<Map<OrderType, Long>> getOrderCountByType() {
        return ResponseEntity.ok(reportService.getOrderCountByType());
    }
}

package com.khanabook.pos.controller;

import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;
import com.khanabook.pos.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/tables")
@RequiredArgsConstructor @Tag(name = "Tables", description = "Table management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TableController {

    private final TableService tableService;

    @PostMapping @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create new table")
    public ResponseEntity<RestaurantTable> createTable(@Valid @RequestBody RestaurantTable table) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.createTable(table));
    }

    @GetMapping @Operation(summary = "Get all tables")
    public ResponseEntity<List<RestaurantTable>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get table by ID")
    public ResponseEntity<RestaurantTable> getTable(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tables by status")
    public ResponseEntity<List<RestaurantTable>> getTablesByStatus(@PathVariable TableStatus status) {
        return ResponseEntity.ok(tableService.getTablesByStatus(status));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update table status")
    public ResponseEntity<RestaurantTable> updateStatus(
            @PathVariable Long id,
            @RequestParam TableStatus status) {
        return ResponseEntity.ok(tableService.updateTableStatus(id, status));
    }

    @GetMapping("/{id}/qr-code")
    @Operation(summary = "Get table QR code")
    public ResponseEntity<String> getQrCode(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.generateQrCode(id));
    }
}

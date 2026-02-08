package com.khanabook.pos.controller;

import com.khanabook.pos.dto.response.KitchenOrderResponse;
import com.khanabook.pos.model.CustomerOrder;
import com.khanabook.pos.service.KitchenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/kitchen")
@RequiredArgsConstructor @PreAuthorize("hasAnyRole('CHEF', 'ADMIN')")
@Tag(name = "Kitchen", description = "Kitchen dashboard endpoints")
@SecurityRequirement(name = "bearerAuth")
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/orders/pending")
    @Operation(summary = "Get pending kitchen orders")
    public ResponseEntity<List<KitchenOrderResponse>> getPendingOrders() {
        return ResponseEntity.ok(kitchenService.getPendingOrders());
    }

    @GetMapping("/orders/active")
    @Operation(summary = "Get active kitchen orders")
    public ResponseEntity<List<KitchenOrderResponse>> getActiveOrders() {
        return ResponseEntity.ok(kitchenService.getActiveOrders());
    }

    @PutMapping("/orders/{id}/ready")
    @Operation(summary = "Mark order as ready")
    public ResponseEntity<CustomerOrder> markReady(@PathVariable Long id) {
        return ResponseEntity.ok(kitchenService.markOrderReady(id));
    }

    @PutMapping("/orders/{id}/kpt")
    @Operation(summary = "Update estimated KPT")
    public ResponseEntity<CustomerOrder> updateKpt(
            @PathVariable Long id,
            @RequestParam Integer minutes) {
        return ResponseEntity.ok(kitchenService.updateKpt(id, minutes));
    }
}

package com.khanabook.pos.controller;

import com.khanabook.pos.dto.request.OrderRequest;
import com.khanabook.pos.dto.request.QrOrderRequest;
import com.khanabook.pos.dto.response.OrderResponse;
import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.service.OrderService;
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

@RestController @RequestMapping("/api/orders")
@RequiredArgsConstructor @Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create manual order (waiter)")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @PostMapping("/qr")
    @Operation(summary = "Create QR-based order (customer)")
    public ResponseEntity<OrderResponse> createQrOrder(@Valid @RequestBody QrOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createQrOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get all orders with pagination")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order (within edit window)")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'CHEF', 'ADMIN')")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel order")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/whatsapp-bill")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
    @Operation(summary = "Send WhatsApp bill")
    public ResponseEntity<Void> sendWhatsAppBill(@PathVariable Long id) {
        orderService.sendWhatsAppBill(id);
        return ResponseEntity.ok().build();
    }
}

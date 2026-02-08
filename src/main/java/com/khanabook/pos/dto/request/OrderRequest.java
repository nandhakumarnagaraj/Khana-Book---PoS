package com.khanabook.pos.dto.request;

import com.khanabook.pos.model.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    private Long tableId; // Required for DINE_IN
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    private String customerPhone;
    private String specialInstructions;
}

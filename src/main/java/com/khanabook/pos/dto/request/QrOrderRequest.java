package com.khanabook.pos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QrOrderRequest {
    
    @NotBlank(message = "QR token is required")
    private String qrToken;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    private String customerPhone;
    private String specialInstructions;
}

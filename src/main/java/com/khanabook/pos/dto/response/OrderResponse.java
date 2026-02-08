package com.khanabook.pos.dto.response;

import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.model.OrderType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private OrderType orderType;
    private OrderStatus status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedReadyTime;
    private Integer estimatedKptMinutes;
    private Boolean isEditable;
    private Boolean isQrOrder;
    private String tableName;
    private List<OrderItemDto> items;
    private Boolean whatsappBillSent;
    
    @Data
    public static class OrderItemDto {
        private Long id;
        private String menuItemName;
        private Integer quantity;
        private Double price;
        private Double subtotal;
        private String specialInstructions;
    }
}

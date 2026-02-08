package com.khanabook.pos.dto.response;

import com.khanabook.pos.model.OrderStatus;
import com.khanabook.pos.model.OrderType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KitchenOrderResponse {
    private Long orderId;
    private String tableName;
    private OrderType orderType;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentToKitchenAt;
    private Integer estimatedKptMinutes;
    private LocalDateTime estimatedReadyTime;
    private List<KitchenItemDto> items;
    private String specialInstructions;
    
    @Data
    public static class KitchenItemDto {
        private String menuItemName;
        private Integer quantity;
        private String specialInstructions;
    }
}

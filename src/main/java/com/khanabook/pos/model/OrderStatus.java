package com.khanabook.pos.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Order placed, awaiting confirmation"),
    CONFIRMED("Order confirmed, ready for kitchen"),
    IN_KITCHEN("Being prepared in kitchen"),
    READY_TO_SERVE("Food ready, waiting to serve"),
    SERVED("Food served to customer"),
    COMPLETED("Order completed and paid"),
    CANCELLED("Order cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}

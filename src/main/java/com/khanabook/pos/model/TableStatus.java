package com.khanabook.pos.model;

import lombok.Getter;

@Getter
public enum TableStatus {
    AVAILABLE("Available for seating"),
    OCCUPIED("Currently occupied"),
    RESERVED("Reserved for booking"),
    CLEANING("Being cleaned"),
    MAINTENANCE("Under maintenance");

    private final String description;

    TableStatus(String description) {
        this.description = description;
    }
}

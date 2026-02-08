package com.khanabook.pos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "restaurant_tables")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantTable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // T1, T2, VIP-1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 4;

    @Column(unique = true, length = 1000)
    private String qrCode; // Base64 QR code image

    @Column(unique = true, nullable = false)
    private String qrToken; // Unique token for scanning

    private String location; // "Ground Floor", "Terrace"
    private String section; // "Smoking", "Non-Smoking"

    @OneToMany(mappedBy = "restaurantTable")
    @JsonIgnore @Builder.Default
    private List<CustomerOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "restaurantTable")
    @JsonIgnore @Builder.Default
    private List<TableBooking> bookings = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (qrToken == null) {
            qrToken = UUID.randomUUID().toString();
        }
    }
}

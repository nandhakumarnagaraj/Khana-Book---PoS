package com.khanabook.pos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "table_bookings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TableBooking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable restaurantTable;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    private String customerEmail;

    @Column(nullable = false)
    private Integer partySize;

    @Column(nullable = false)
    private LocalDateTime bookingDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private String specialRequests;

    @OneToOne @JoinColumn(name = "order_id")
    private CustomerOrder customerOrder;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.khanabook.pos.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime sentToKitchenAt;
    private LocalDateTime readyAt;
    private LocalDateTime servedAt;
    private LocalDateTime completedAt;

    // For 2-minute edit window (QR orders)
    private LocalDateTime editableUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEditable = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isQrOrder = false;

    // KPT tracking
    private Integer estimatedKptMinutes;
    private Integer actualKptMinutes;
    private LocalDateTime estimatedReadyTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_table_id")
    private RestaurantTable restaurantTable;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // WhatsApp
    @Builder.Default
    private Boolean whatsappBillSent = false;
    private String customerPhone;

    private String specialInstructions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isQrOrder) {
            editableUntil = createdAt.plusMinutes(2);
        }
    }

    public boolean isEditAllowed() {
        if (!isEditable)
            return false;
        if (editableUntil == null)
            return true;
        return LocalDateTime.now().isBefore(editableUntil);
    }

    public void calculateEstimatedReadyTime() {
        if (estimatedKptMinutes != null && sentToKitchenAt != null) {
            estimatedReadyTime = sentToKitchenAt.plusMinutes(estimatedKptMinutes);
        }
    }
}

package com.khanabook.pos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "kitchen_preparation_times")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KitchenPreparationTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "menu_item_id", unique = true, nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    @Builder.Default
    private Integer estimatedMinutes = 15;

    @Column(nullable = false)
    @Builder.Default
    private Integer minMinutes = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxMinutes = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_by_user_id")
    private User setBy; // Chef who set this

    private LocalDateTime lastUpdated;

    @PrePersist @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

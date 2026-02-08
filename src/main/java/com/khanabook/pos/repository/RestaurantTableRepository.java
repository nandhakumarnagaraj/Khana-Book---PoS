package com.khanabook.pos.repository;

import com.khanabook.pos.model.RestaurantTable;
import com.khanabook.pos.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByQrToken(String qrToken);
    List<RestaurantTable> findByStatus(TableStatus status);
    Boolean existsByName(String name);
}

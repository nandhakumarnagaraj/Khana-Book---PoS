package com.khanabook.pos.repository;

import com.khanabook.pos.model.KitchenPreparationTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KptRepository extends JpaRepository<KitchenPreparationTime, Long> {
    Optional<KitchenPreparationTime> findByMenuItemId(Long menuItemId);
}

package com.khanabook.pos.repository;

import com.khanabook.pos.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByAvailableTrue(Pageable pageable);
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    Page<MenuItem> findByCategoryId(Long categoryId, Pageable pageable);
}

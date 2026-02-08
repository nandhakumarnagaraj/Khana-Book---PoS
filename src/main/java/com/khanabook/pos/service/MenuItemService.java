package com.khanabook.pos.service;

import com.khanabook.pos.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MenuItemService {

	MenuItem createMenuItem(MenuItem menuItem);

	Optional<MenuItem> getMenuItemById(Long id);

	Page<MenuItem> getAllMenuItems(Pageable pageable);

	Page<MenuItem> getAvailableMenuItems(Pageable pageable);

	List<MenuItem> getMenuItemsByCategoryId(Long categoryId);

	Page<MenuItem> getMenuItemsByCategoryIdPaged(Long categoryId, Pageable pageable);

	MenuItem updateMenuItem(Long id, MenuItem menuItem);

	void deleteMenuItem(Long id);

	MenuItem toggleMenuItemAvailability(Long id, boolean available);
}

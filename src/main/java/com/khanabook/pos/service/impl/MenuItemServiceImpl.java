package com.khanabook.pos.service.impl;

import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.Category;
import com.khanabook.pos.model.MenuItem;
import com.khanabook.pos.repository.CategoryRepository;
import com.khanabook.pos.repository.MenuItemRepository;
import com.khanabook.pos.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public MenuItem createMenuItem(MenuItem menuItem) {
        if (menuItem.getCategory() == null || menuItem.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        Category category = categoryRepository.findById(menuItem.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + menuItem.getCategory().getId()));
        menuItem.setCategory(category);
        menuItem.setCreatedAt(LocalDateTime.now());
        return menuItemRepository.save(menuItem);
    }

    @Override
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    @Override
    @Cacheable("menuItems")
    public Page<MenuItem> getAllMenuItems(Pageable pageable) {
        return menuItemRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'available_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MenuItem> getAvailableMenuItems(Pageable pageable) {
        return menuItemRepository.findByAvailableTrue(pageable);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'category_' + #categoryId")
    public List<MenuItem> getMenuItemsByCategoryId(Long categoryId) {
        return menuItemRepository.findByCategoryIdAndAvailableTrue(categoryId);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'category_paged_' + #categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MenuItem> getMenuItemsByCategoryIdPaged(Long categoryId, Pageable pageable) {
        return menuItemRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public MenuItem updateMenuItem(Long id, MenuItem updatedMenuItem) {
        MenuItem existingMenuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + id));

        Category category = categoryRepository.findById(updatedMenuItem.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + updatedMenuItem.getCategory().getId()));

        existingMenuItem.setName(updatedMenuItem.getName());
        existingMenuItem.setDescription(updatedMenuItem.getDescription());
        existingMenuItem.setPrice(updatedMenuItem.getPrice());
        existingMenuItem.setCategory(category);
        existingMenuItem.setImageLob(updatedMenuItem.getImageLob());
        existingMenuItem.setAvailable(updatedMenuItem.getAvailable());
        existingMenuItem.setVegetarian(updatedMenuItem.getVegetarian());
        existingMenuItem.setVegan(updatedMenuItem.getVegan());
        existingMenuItem.setSpiceLevel(updatedMenuItem.getSpiceLevel());

        return menuItemRepository.save(existingMenuItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu Item not found with id: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuItems", allEntries = true)
    public MenuItem toggleMenuItemAvailability(Long id, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found with id: " + id));
        menuItem.setAvailable(available);
        return menuItemRepository.save(menuItem);
    }
}

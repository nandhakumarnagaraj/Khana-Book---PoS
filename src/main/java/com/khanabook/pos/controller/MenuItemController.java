package com.khanabook.pos.controller;

import com.khanabook.pos.model.MenuItem;
import com.khanabook.pos.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/menu-items")
@RequiredArgsConstructor @Tag(name = "Menu Items", description = "Menu item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    @Operation(summary = "Get all menu items with pagination")
    public ResponseEntity<Page<MenuItem>> getAllMenuItems(Pageable pageable) {
        return ResponseEntity.ok(menuItemService.getAllMenuItems(pageable));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available menu items with pagination")
    public ResponseEntity<Page<MenuItem>> getAvailableMenuItems(Pageable pageable) {
        return ResponseEntity.ok(menuItemService.getAvailableMenuItems(pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get menu items by category ID (only available items)")
    public ResponseEntity<List<MenuItem>> getMenuItemsByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByCategoryId(categoryId));
    }

    @GetMapping("/category/{categoryId}/paged")
    @Operation(summary = "Get menu items by category ID with pagination")
    public ResponseEntity<Page<MenuItem>> getMenuItemsByCategoryIdPaged(@PathVariable Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByCategoryIdPaged(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new menu item")
    public ResponseEntity<MenuItem> createMenuItem(@Valid @RequestBody MenuItem menuItem) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.createMenuItem(menuItem));
    }

    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update an existing menu item")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, menuItem));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability") @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Toggle menu item availability")
    public ResponseEntity<MenuItem> toggleMenuItemAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return ResponseEntity.ok(menuItemService.toggleMenuItemAvailability(id, available));
    }
}

package com.khanabook.pos.controller;

import com.khanabook.pos.dto.request.CreateUserRequest;
import com.khanabook.pos.dto.response.UserResponse;
import com.khanabook.pos.model.User;
import com.khanabook.pos.service.UserManagementService;
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

 @RestController @RequestMapping("/api/users")
 @RequiredArgsConstructor @Tag(name = "User Management", description = "Admin user management endpoints")
 @SecurityRequirement(name = "bearerAuth")
 @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @PostMapping @Operation(summary = "Create new user (Admin only)")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createUser(request));
    }

    @GetMapping @Operation(summary = "Get all users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById( @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long id, 
            @RequestBody String role) {
        return ResponseEntity.ok(userManagementService.updateUserRoles(id, role));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user")
    public ResponseEntity<UserResponse> activateUser( @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<UserResponse> deactivateUser( @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.deactivateUser(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser( @PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

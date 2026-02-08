package com.khanabook.pos.service.impl;

import com.khanabook.pos.dto.request.CreateUserRequest;
import com.khanabook.pos.dto.response.UserResponse;
import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.Role;
import com.khanabook.pos.model.User;
import com.khanabook.pos.repository.UserRepository;
import com.khanabook.pos.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Role role = Role.valueOf(request.getRole());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .role(role)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToUserResponse(user);
    }

    // ... other methods ...

    // Helper method to convert User to UserResponse
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override @Transactional
    public UserResponse updateUserRoles(Long id, String roleString) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Role role = Role.valueOf(roleString);
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(true);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}

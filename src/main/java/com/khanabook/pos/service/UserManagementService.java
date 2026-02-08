package com.khanabook.pos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.khanabook.pos.dto.request.CreateUserRequest;
import com.khanabook.pos.dto.response.UserResponse;
import com.khanabook.pos.model.User;

public interface UserManagementService {

	User createUser(CreateUserRequest request);

	Page<UserResponse> getAllUsers(Pageable pageable);

	UserResponse getUserById(Long id);

	UserResponse updateUserRoles(Long id, String role);

	UserResponse activateUser(Long id);

	UserResponse deactivateUser(Long id);

	void deleteUser(Long id);
}

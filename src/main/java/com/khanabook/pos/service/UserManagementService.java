package com.khanabook.pos.service;

import com.khanabook.pos.dto.request.CreateUserRequest;
import com.khanabook.pos.dto.response.UserResponse;
import com.khanabook.pos.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserManagementService {

	User createUser(CreateUserRequest request);

	Page<UserResponse> getAllUsers(Pageable pageable);

	UserResponse getUserById(Long id);

	UserResponse updateUserRoles(Long id, String role);

	UserResponse activateUser(Long id);

	UserResponse deactivateUser(Long id);

	void deleteUser(Long id);
}

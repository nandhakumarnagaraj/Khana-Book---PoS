package com.khanabook.pos.dto.response;

import java.time.LocalDateTime;

import com.khanabook.pos.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

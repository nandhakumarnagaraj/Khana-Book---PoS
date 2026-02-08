package com.khanabook.pos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

 @Data
public class CreateUserRequest {
    
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank @Size(min = 8, max = 100)
    private String password;
    
    @Email
    private String email;
    
    private String phoneNumber;
    
    private String fullName;
    
    @NotBlank(message = "Role is required")
    private String role;
}

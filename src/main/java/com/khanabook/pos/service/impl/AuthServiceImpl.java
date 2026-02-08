package com.khanabook.pos.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khanabook.pos.dto.request.AuthRequest;
import com.khanabook.pos.dto.request.RegisterRequest;
import com.khanabook.pos.dto.response.AuthResponse;
import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.Role;
import com.khanabook.pos.model.User;
import com.khanabook.pos.repository.UserRepository;
import com.khanabook.pos.security.JwtUtil;
import com.khanabook.pos.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .role(Role.WAITER) // Default role
                .active(true)
                .build();

        user = userRepository.save(user);

        // Generate token immediately
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name());
    }

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name());
    }
}

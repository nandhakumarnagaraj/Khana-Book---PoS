package com.khanabook.pos.service;

import com.khanabook.pos.dto.request.AuthRequest;
import com.khanabook.pos.dto.request.RegisterRequest;
import com.khanabook.pos.dto.response.AuthResponse;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse authenticate(AuthRequest request);
}

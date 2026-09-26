package com.akshay.service;

import com.akshay.dto.auth.AuthResponse;
import com.akshay.dto.auth.LoginRequest;
import com.akshay.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

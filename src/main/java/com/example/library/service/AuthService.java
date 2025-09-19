package com.example.library.service;

import com.example.library.dto.request.auth.LoginRequest;
import com.example.library.dto.response.auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String email);
    LoginResponse refreshToken(String request);
}

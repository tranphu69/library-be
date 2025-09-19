package com.example.library.controller;

import com.example.library.dto.request.auth.LoginRequest;
import com.example.library.dto.request.auth.RefreshTokenRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.auth.LoginResponse;
import com.example.library.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(authService.login(request));
        return apiResponse;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Logout successful!");
        return apiResponse;
    }

    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(authService.refreshToken(request.getRefreshToken()));
        return apiResponse;
    }
}

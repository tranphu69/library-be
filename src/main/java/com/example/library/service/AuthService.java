package com.example.library.service;

import com.example.library.dto.request.ChangePasswordRequest;
import com.example.library.dto.request.LoginRequest;
import com.example.library.dto.request.RefreshTokenRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.AuthMeResponse;
import com.example.library.dto.response.LoginResponse;
import com.example.library.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void signUp(UserRequest request);
    void verifyEmail(String token);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    LoginResponse refreshToken(RefreshTokenRequest request);
    User me (String username);
    void logout(String username);
    void changePassword(ChangePasswordRequest request, String username);
}

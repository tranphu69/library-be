package com.example.library.controller;

import com.example.library.dto.request.LoginRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.LoginResponse;
import com.example.library.service.AuthService;
import com.example.library.service.UserService;
import com.example.library.validation.OnSignUp;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(authService.login(request));
        return apiResponse;
    }

    @PostMapping("/signUp")
    public ApiResponse<String> signUp(@Validated(OnSignUp.class) @RequestBody UserRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        authService.signUp(request);
        apiResponse.setMessage("Cần vào email để kích hoạt tài khoản này!");
        return apiResponse;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Xác thực thành công");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Cần vào email để đặt lại mất khẩu!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }
}

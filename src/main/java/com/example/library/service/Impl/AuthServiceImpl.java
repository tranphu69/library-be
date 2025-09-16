package com.example.library.service.Impl;

import com.example.library.dto.request.auth.LoginRequest;
import com.example.library.dto.response.auth.LoginResponse;
import com.example.library.entity.User;
import com.example.library.enums.AuthErrorCode;
import com.example.library.enums.UserErrorCode;
import com.example.library.exception.AppException;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(UserErrorCode.USER_USERNAME_EXSITED));

            return null;
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.AUTH_ERROR_CODE);
        }
    }
}

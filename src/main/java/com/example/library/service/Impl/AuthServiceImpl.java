package com.example.library.service.Impl;

import com.example.library.dto.request.LoginRequest;
import com.example.library.dto.response.LoginResponse;
import com.example.library.dto.response.User.UserResponseListRole;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.messageError.ErrorCode;
import com.example.library.exception.messageError.UserErrorCode;
import com.example.library.repository.UserRepository;
import com.example.library.security.JwtTokenProvider;
import com.example.library.service.AuthService;
import com.example.library.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AppException(UserErrorCode.USER_USERNAME_EXSITED));
            List<String> nameRole = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();
            String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername(), nameRole);
            String refreshToken = "";
            UserResponseListRole profile = new UserResponseListRole();
            profile.setUsername(user.getUsername());
            profile.setId(user.getId());
            profile.setEmail(user.getEmail());
            profile.setRoles(nameRole);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(accessToken);
            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setTokenType("Bearer");
            loginResponse.setUser(profile);
            return loginResponse;
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTH_ERROR_CODE);
        }
    }
}

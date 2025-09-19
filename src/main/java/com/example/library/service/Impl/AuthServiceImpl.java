package com.example.library.service.Impl;

import com.example.library.dto.request.auth.LoginRequest;
import com.example.library.dto.response.auth.LoginResponse;
import com.example.library.dto.response.user.UserResponseListRole;
import com.example.library.entity.Permission;
import com.example.library.entity.RefreshToken;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.enums.AuthErrorCode;
import com.example.library.enums.UserErrorCode;
import com.example.library.exception.AppException;
import com.example.library.repository.UserRepository;
import com.example.library.security.JwtTokenProvider;
import com.example.library.service.AuthService;
import com.example.library.service.auth.RefreshTokenService;
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
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(UserErrorCode.USER_USERNAME_EXSITED));
            List<String> roles = user.getRoles().stream()
                    .map(role -> "ROLE_" + role.getName())
                    .toList();
            List<String> permissions = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getName)
                    .distinct()
                    .toList();
            String accessToken = jwtTokenProvider.generateAccessToken(request.getEmail(), roles);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
            UserResponseListRole profile = new UserResponseListRole();
            profile.setEmail(user.getEmail());
            profile.setId(user.getId());
            profile.setUsername(user.getUsername());
            profile.setRoles(roles);
            profile.setPermissions(permissions);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(accessToken);
            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setTokenType("Bearer");
            loginResponse.setUser(profile);
            return loginResponse;
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.AUTH_ERROR_CODE);
        }
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        refreshTokenService.deleteByUser(user);
    }

    @Override
    public LoginResponse refreshToken(String request) {
        RefreshToken token = refreshTokenService.findByToken(request)
                .orElseThrow(() -> new AppException(AuthErrorCode.AUTH_INVALID_TOKEN));
        if(refreshTokenService.isTokenExpired(token)){
            refreshTokenService.deleteByToken(request);
            throw new AppException(AuthErrorCode.AUTH_EXPIRED_TOKEN);
        }
        List<String> roles = token.getUser().getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();
        List<String> permissions = token.getUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();
        String accessToken = jwtTokenProvider.generateAccessToken(token.getUser().getEmail(), roles);
        String newRefreshToken = null;
        if (refreshTokenService.isTokenExpiringSoon(token, 7)) {
            newRefreshToken = refreshTokenService.createRefreshToken(token.getUser()).getToken();
            refreshTokenService.deleteByToken(request);
        }
        UserResponseListRole profile = new UserResponseListRole();
        profile.setEmail(token.getUser().getEmail());
        profile.setId(token.getUser().getId());
        profile.setUsername(token.getUser().getUsername());
        profile.setRoles(roles);
        profile.setPermissions(permissions);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(newRefreshToken != null ? newRefreshToken : request);
        loginResponse.setTokenType("Bearer");
        loginResponse.setUser(profile);
        return loginResponse;
    }
}

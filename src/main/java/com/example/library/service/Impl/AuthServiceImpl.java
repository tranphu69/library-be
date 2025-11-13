package com.example.library.service.Impl;

import com.example.library.dto.request.ChangePasswordRequest;
import com.example.library.dto.request.LoginRequest;
import com.example.library.dto.request.RefreshTokenRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.AuthMeResponse;
import com.example.library.dto.response.LoginResponse;
import com.example.library.dto.response.User.UserResponseListRole;
import com.example.library.entity.*;
import com.example.library.enums.AccountStatus;
import com.example.library.enums.Position;
import com.example.library.enums.TypeToken;
import com.example.library.exception.AppException;
import com.example.library.exception.messageError.ErrorCode;
import com.example.library.exception.messageError.RoleErrorCode;
import com.example.library.exception.messageError.UserErrorCode;
import com.example.library.repository.RefreshTokenRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.repository.VerificationTokenRepository;
import com.example.library.security.JwtTokenProvider;
import com.example.library.service.AuthService;
import com.example.library.service.OtherService.EmailService;
import com.example.library.service.OtherService.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private void sendEmailVerification(User user) {
        verificationTokenRepository.deleteByUserAndTypeToken(user, TypeToken.VERIFY_EMAIL);
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setTypeToken(TypeToken.VERIFY_EMAIL);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);
        String verifyLink = "http://localhost:8080/api/v1/auth/verify?token=" + token;
        emailService.sendSimpleEmail(
                user.getEmail(),
                "Xác thực tài khoản của bạn",
                "Chào " + user.getUsername() + ",\n\n" +
                        "Vui lòng bấm vào link sau để xác thực tài khoản:\n" + verifyLink + "\n\n" +
                        "Link này sẽ hết hạn sau 24 giờ."
        );
    }

    public AuthServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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
                    .map(role -> "ROLE_" + role.getName())
                    .toList();
            List<String> namePermission = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getName)
                    .toList();
            String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername(), nameRole);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
            UserResponseListRole profile = new UserResponseListRole();
            profile.setUsername(user.getUsername());
            profile.setId(user.getId());
            profile.setEmail(user.getEmail());
            profile.setRoles(nameRole);
            profile.setPermissions(namePermission);
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

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_TOKEN_INVALID));
        if(refreshTokenService.isTokenExpired(token)) {
            refreshTokenRepository.deleteByToken(request.getRefreshToken());
            throw new AppException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }
        List<String> roleNames = token.getUser().getRoles()
                .stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();
        List<String> namePermission = token.getUser().getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .toList();
        String accessToken = jwtTokenProvider.generateAccessToken(token.getUser().getUsername(), roleNames);
        String newRefreshToken = null;
        if(refreshTokenService.isTokenExpiringSoon(token, 3)) {
            newRefreshToken = refreshTokenService.createRefreshToken(token.getUser()).getToken();
            refreshTokenRepository.deleteByToken(request.getRefreshToken());
        }
        UserResponseListRole profile = new UserResponseListRole();
        profile.setUsername(token.getUser().getUsername());
        profile.setId(token.getUser().getId());
        profile.setEmail(token.getUser().getEmail());
        profile.setRoles(roleNames);
        profile.setPermissions(namePermission);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(newRefreshToken != null ? newRefreshToken : request.getRefreshToken());
        loginResponse.setTokenType("Bearer");
        loginResponse.setUser(profile);
        return loginResponse;
    }

    @Override
    public User me(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
    }

    @Override
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        if (!request.getNewPassword().trim().equals(request.getConfirmPassword().trim())) {
            throw new AppException(ErrorCode.AUTH_NOT_CHECK_PASSWORD);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.AUTH_WRONG_PASSWORD);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.AUTH_SAME_PASSWORD);
        }
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void signUp(UserRequest request) {
        String newUsername = request.getUsername().trim();
        if (userRepository.existsByUsername(newUsername)) {
            throw new AppException(UserErrorCode.USER_USERNAME_EXSITED);
        }
        String newEmail = request.getEmail().trim();
        if (userRepository.existsByEmail(newEmail)) {
            throw new AppException(UserErrorCode.USER_EMAIL_EXSITED);
        }
        String newPhone = request.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            newPhone = newPhone.trim();
            if (userRepository.existsByPhone(newPhone)) {
                throw new AppException(UserErrorCode.USER_PHONE_EXSITED);
            }
        }
        String newCode = request.getCode();
        if (newCode != null && !newCode.trim().isEmpty()) {
            newCode = newCode.trim();
            if (userRepository.existsByCode(newCode)) {
                throw new AppException(UserErrorCode.USER_CODE_EXSITED);
            }
        }
        if (!Objects.equals(request.getPassword(), request.getPasswordConfirm())) {
            throw new AppException(ErrorCode.AUTH_CHECK_PASSWORD);
        }
        Role roleUser = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NO_EXSITED));
        User user = new User();
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        user.setCode(request.getCode() != null ? request.getCode().trim() : null);
        user.setPhone(newPhone);
        user.setMajor(request.getMajor() != null ? request.getMajor().trim() : null);
        user.setCourse(request.getCourse() != null ? request.getCourse().trim() : null);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPosition(Position.STUDENT);
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setStatus(AccountStatus.VERIFICATION);
        user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        user.setRoles(Set.of(roleUser));
        userRepository.save(user);
        sendEmailVerification(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository
                .findByTokenAndTypeToken(token, TypeToken.VERIFY_EMAIL);
        if (optionalToken.isEmpty()) {
            throw new AppException(ErrorCode.AUTH_TOKEN_INVALID);
        }
        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }
        User user = verificationToken.getUser();
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (!newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$")) {
            throw new AppException(UserErrorCode.USER_PASSWORD_CHARACTER);
        }
        VerificationToken resetToken = verificationTokenRepository
                .findByTokenAndTypeToken(token, TypeToken.RESET_PASSWORD)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_TOKEN_INVALID));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.AUTH_TOKEN_EXPIRED);
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        verificationTokenRepository.delete(resetToken);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        verificationTokenRepository.deleteByUserAndTypeToken(user, TypeToken.RESET_PASSWORD);
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setTypeToken(TypeToken.RESET_PASSWORD);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        verificationTokenRepository.save(resetToken);
        String resetLink = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;
        emailService.sendSimpleEmail(
                user.getEmail(),
                "Đặt lại mật khẩu",
                "Chào " + user.getUsername() + ",\n\n" +
                        "Bấm vào link sau để đặt lại mật khẩu:\n" + resetLink + "\n\n" +
                        "Link này sẽ hết hạn sau 1 giờ.\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này."
        );
    }
}

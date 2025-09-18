package com.example.library.service.auth;

import com.example.library.entity.RefreshToken;
import com.example.library.entity.User;
import com.example.library.repository.RefreshTokenRepository;
import com.example.library.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenDurationMs = refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtTokenProvider.generateRefreshToken(user.getEmail()));
        refreshToken.setExpiredAt(LocalDateTime.now().plus(refreshTokenDurationMs, ChronoUnit.MILLIS));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }
}

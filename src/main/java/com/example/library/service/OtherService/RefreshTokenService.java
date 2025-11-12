package com.example.library.service.OtherService;

import com.example.library.entity.RefreshToken;
import com.example.library.repository.RefreshTokenRepository;
import com.example.library.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.library.entity.User;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiredAt().isBefore(LocalDateTime.now());
    }

    public boolean isTokenExpiringSoon(RefreshToken token, long days) {
        LocalDateTime expiryThreshold = LocalDateTime.now().plus(Duration.ofDays(days));
        return token.getExpiredAt().isBefore(expiryThreshold);
    }

    public RefreshTokenService(JwtTokenProvider jwtTokenProvider, @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays, RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenDurationMs = refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtTokenProvider.generateRefreshToken(user.getUsername()));
        refreshToken.setRevoked(false);
        refreshToken.setExpiredAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenDurationMs)));
        return refreshTokenRepository.save(refreshToken);
    }
}

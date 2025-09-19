package com.example.library.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;
    @Value("${jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMinutes * 60 * 1000;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
    }

    public String getSecret() {
        return secret;
    }
}

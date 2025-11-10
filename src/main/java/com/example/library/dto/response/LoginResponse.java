package com.example.library.dto.response;

import com.example.library.dto.response.User.UserResponse;
import com.example.library.dto.response.User.UserResponseListRole;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponseListRole user;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponseListRole getUser() {
        return user;
    }

    public void setUser(UserResponseListRole user) {
        this.user = user;
    }
}

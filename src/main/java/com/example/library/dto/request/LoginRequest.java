package com.example.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "USER_USERNAME_EMPTY")
    @Size(max = 100, message = "USER_USERNAME_EXCEED")
    private String username;
    @NotBlank(message = "USER_PASSWORD_EMPTY")
    @Size(min = 10, max = 16, message = "USER_PASSWORD_EXCEED")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$",
            message = "USER_PASSWORD_CHARACTER"
    )
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package com.example.library.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UserCreateRequest {
    @NotBlank(message = "USER_USERNAME_REQUEST")
    @Size(max = 50, message = "USER_USERNAME_MAX_LENGTH")
    private String username;
    @NotBlank(message = "USER_EMAIL_REQUEST")
    @Size(max = 50, message = "USER_EMAIL_MAX_LENGTH")
    private String email;
    @NotBlank(message = "USER_PASSWORD_REQUEST")
    @Size(min = 8, message = "USER_PASSWORD_MIN_LENGTH")
    private String password;
    private Integer isActive;
    private List<Long> roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public List<Long> getRoles() {
        return roles;
    }

    public void setRoles(List<Long> roles) {
        this.roles = roles;
    }
}

package com.example.library.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UserUpdateRequest {
    private Long id;
    @NotBlank(message = "USERNAME_REQUEST")
    @Size(max = 50, message = "USERNAME_MAX_LENGTH")
    private String username;
    @NotBlank(message = "EMAIL_REQUEST")
    @Size(max = 50, message = "EMAIL_MAX_LENGTH")
    private String email;
    private Integer isActive;
    private List<Long> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

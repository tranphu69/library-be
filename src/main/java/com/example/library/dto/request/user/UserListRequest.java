package com.example.library.dto.request.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserListRequest {
    @Size(max = 50, message = "USER_USERNAME_MAX_LENGTH")
    private String email;
    @Size(max = 50, message = "USER_EMAIL_MAX_LENGTH")
    private String username;
    @Min(value = 0, message = "USER_INVALID")
    @Max(value = 1, message = "USER_INVALID")
    private Integer isActive;
    @Min(value = 0, message = "USER_INVALID")
    private Integer page;
    @Min(value = 1, message = "USER_INVALID")
    private Integer size;
    private String sortBy;
    @Pattern(regexp = "^(ASC|DESC)$", message = "USER_INVALID")
    private String sortType;
    private String roles;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}

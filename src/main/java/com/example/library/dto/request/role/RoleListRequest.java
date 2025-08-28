package com.example.library.dto.request.role;

import jakarta.validation.constraints.*;

import java.util.List;

public class RoleListRequest {
    @Size(max = 50, message = "NAME_MAX_LENGTH")
    private String name;
    @Min(value = 0, message = "INVALID")
    @Max(value = 1, message = "INVALID")
    private Integer status;
    @Min(value = 0, message = "INVALID")
    private Integer page;
    @Min(value = 1, message = "INVALID")
    private Integer size;
    private String sortBy;
    @Pattern(regexp = "^(ASC|DESC)$", message = "INVALID")
    private String sortType;
    private String permissions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}

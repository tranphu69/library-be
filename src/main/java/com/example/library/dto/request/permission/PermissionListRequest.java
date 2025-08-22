package com.example.library.dto.request.permission;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PermissionListRequest {
    @Size(max = 50, message = "PERMISSION_NAME_MAX_LENGTH")
    private String name;
    @Min(value = 0, message = "PERMISSION_INVALID")
    @Max(value = 1, message = "PERMISSION_INVALID")
    private Integer status;
    @Min(value = 0, message = "PERMISSION_INVALID")
    private Integer page;
    @Min(value = 1, message = "PERMISSION_INVALID")
    private Integer size;
    private String sortBy;
    @Pattern(regexp = "^(ASC|DESC)$", message = "PERMISSION_INVALID")
    private String sortType;

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
}

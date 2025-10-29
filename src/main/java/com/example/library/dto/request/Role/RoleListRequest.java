package com.example.library.dto.request.Role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RoleListRequest {
    @Size(max = 100, message = "ROLE_NAME_EXCEED")
    private String name;
    @Min(value = 0, message = "ROLE_VALUE")
    @Max(value = 1, message = "ROLE_VALUE")
    private Integer action;
    @Min(value = 0, message = "ROLE_VALUE")
    private Integer page;
    @Min(value = 1, message = "ROLE_VALUE")
    private Integer size;
    private String sortBy;
    @Pattern(regexp = "^(ASC|DESC)$", message = "ROLE_VALUE")
    private String sortType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
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

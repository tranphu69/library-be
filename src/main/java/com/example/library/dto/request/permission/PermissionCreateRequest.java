package com.example.library.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PermissionCreateRequest {
    @NotBlank(message = "PERMISSION_NAME_REQUEST")
    private String name;
    private String description;
    @NotNull(message = "PERMISSION_STATUS_REQUEST")
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

package com.example.library.dto.request.role;

import jakarta.validation.constraints.*;

import java.util.List;

public class RoleCreateRequest {
    @NotBlank(message = "ROLE_NAME_REQUEST")
    @Size(max = 50, message = "ROLE_NAME_MAX_LENGTH")
    private String name;
    @Size(max = 255, message = "ROLE_DESCRIPTION_MAX_LENGTH")
    private String description;
    @NotNull(message = "ROLE_STATUS_REQUEST")
    @Min(value = 0, message = "ROLE_INVALID_STATUS")
    @Max(value = 1, message = "ROLE_INVALID_STATUS")
    private Integer status;
    private List<Long> permissions;

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

    public List<Long> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Long> permissions) {
        this.permissions = permissions;
    }
}

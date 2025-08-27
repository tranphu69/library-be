package com.example.library.dto.request.role;

import jakarta.validation.constraints.*;

import java.util.List;

public class RoleCreateRequest {
    @NotBlank(message = "NAME_REQUEST")
    @Size(max = 50, message = "NAME_MAX_LENGTH")
    private String name;
    @Size(max = 255, message = "DESCRIPTION_MAX_LENGTH")
    private String description;
    @NotNull(message = "STATUS_REQUEST")
    @Min(value = 0, message = "INVALID")
    @Max(value = 1, message = "INVALID")
    private Integer status;
    @NotEmpty(message = "PERMISSIONS_REQUEST")
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

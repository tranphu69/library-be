package com.example.library.dto.request.Role;

import jakarta.validation.constraints.*;

import java.util.List;

public class RoleRequest {
    @NotBlank(message = "ROLE_NAME_EMPTY")
    @Size(max = 100, message = "ROLE_NAME_EXCEED")
    private String name;
    @Size(max = 255, message = "ROLE_DESCRIPTION_EXCEED")
    private String description;
    @NotNull(message = "ROLE_ACTION_EMPTY")
    @Min(value = 0, message = "ROLE_VALUE")
    @Max(value = 1, message = "ROLE_VALUE")
    private Integer action;
    @NotNull(message = "ROLE_PERMISSION_EMPTY")
    @Size(min = 1, message = "ROLE_PERMISSION_EMPTY")
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

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public List<Long> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Long> permissions) {
        this.permissions = permissions;
    }
}

package com.example.library.dto.request.Permission;

import jakarta.validation.constraints.*;

public class PermissionRequest {
    @NotBlank(message = "PERMISSION_NAME_EMPTY")
    @Size(max = 100, message = "PERMISSION_NAME_EXCEED")
    private String name;
    @Size(max = 255, message = "PERMISSION_DESCRIPTION_EXCEED")
    private String description;
    @NotNull(message = "PERMISSION_ACTION_EMPTY")
    @Min(value = 0, message = "PERMISSION_VALUE")
    @Max(value = 1, message = "PERMISSION_VALUE")
    private Short action;

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

    public Short getAction() {
        return action;
    }

    public void setAction(Short action) {
        this.action = action;
    }
}

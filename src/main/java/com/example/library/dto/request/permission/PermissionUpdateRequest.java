package com.example.library.dto.request.permission;

import jakarta.validation.constraints.*;

public class PermissionUpdateRequest {
    private Long id;
    @NotBlank(message = "PERMISSION_NAME_REQUEST")
    @Size(max = 50, message = "PERMISSION_NAME_MAX_LENGTH")
    private String name;
    @Size(max = 255, message = "PERMISSION_DESCRIPTION_MAX_LENGTH")
    private String description;
    @NotNull(message = "PERMISSION_STATUS_REQUEST")
    @Min(value = 0, message = "PERMISSION_INVALID")
    @Max(value = 1, message = "PERMISSION_INVALID")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}

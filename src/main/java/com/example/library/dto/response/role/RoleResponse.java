package com.example.library.dto.response.role;

import com.example.library.dto.response.permission.PermissionNoStatus;

import java.util.Set;

public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private Set<PermissionNoStatus> permissions;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<PermissionNoStatus> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionNoStatus> permissions) {
        this.permissions = permissions;
    }
}

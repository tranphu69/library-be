package com.example.library.service;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.entity.Role;

public interface RoleService {
    Role create(RoleCreateRequest request);
    Role update(RoleUpdateRequest request);
}

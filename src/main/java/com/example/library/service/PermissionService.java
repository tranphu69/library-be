package com.example.library.service;

import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.entity.Permission;

public interface PermissionService {
    Permission create(PermissionRequest request);
}

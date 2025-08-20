package com.example.library.service;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.entity.Permission;

import java.util.List;

public interface PermissionService {
    Permission create(PermissionCreateRequest request);
    Permission update(PermissionUpdateRequest request);
    PermissionListResponse getList(PermissionListRequest request);
    void delete (Long id);
}

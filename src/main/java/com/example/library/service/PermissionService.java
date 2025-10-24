package com.example.library.service;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.entity.Permission;

import java.util.List;

public interface PermissionService {
    Permission create(PermissionRequest request);
    Permission update(PermissionRequest request, Long id);
    void delete(List<Long> id);
    Permission detail(Long id);
    PermissionListResponse getList(PermissionListRequest request);
}

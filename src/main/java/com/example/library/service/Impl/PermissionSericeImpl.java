package com.example.library.service.Impl;

import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.entity.Permission;
import com.example.library.exception.AppException;
import com.example.library.exception.enums.PermissionErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionSericeImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Permission create(PermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new AppException(PermissionErrorCode.PERMISSION_EXSITED);
        }
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setAction(request.getAction());
        return permissionRepository.save(permission);
    }
}

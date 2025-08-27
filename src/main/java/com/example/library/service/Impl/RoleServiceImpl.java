package com.example.library.service.Impl;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Role create(RoleCreateRequest request) {
        if(roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXSITED);
        }
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    public Role update(RoleUpdateRequest request) {
        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXSITED));
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }
}

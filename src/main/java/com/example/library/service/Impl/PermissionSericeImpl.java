package com.example.library.service.Impl;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.exception.AppException;
import com.example.library.exception.enums.PermissionErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import com.example.library.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PermissionSericeImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    private List<PermissionResponse> getListPermission(Page<Permission> permissionPage) {
        return permissionPage.getContent()
                .stream()
                .map(permission -> {
                    PermissionResponse response = new PermissionResponse();
                    response.setId(permission.getId());
                    response.setName(permission.getName());
                    response.setDescription(permission.getDescription());
                    response.setAction(permission.getAction());
                    response.setCreatedAt(permission.getCreatedAt());
                    response.setUpdatedAt(permission.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Permission create(PermissionRequest request) {
        String newName = request.getName().trim();
        if (permissionRepository.existsByName(newName)) {
            throw new AppException(PermissionErrorCode.PERMISSION_EXSITED);
        }
        Permission permission = new Permission();
        permission.setName(newName);
        permission.setDescription(request.getDescription());
        permission.setAction(request.getAction());
        return permissionRepository.save(permission);
    }

    @Override
    public Permission update(PermissionRequest request, Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED));
        String newName = request.getName().trim();
        if (permissionRepository.existsByNameAndIdNot(newName, id)) {
            throw new AppException(PermissionErrorCode.PERMISSION_EXSITED);
        }
        if (Objects.equals(permission.getName(), newName) && Objects.equals(permission.getDescription(), request.getDescription())
                && Objects.equals(permission.getAction(), request.getAction())) {
            return permission;
        }
        permission.setName(newName);
        permission.setDescription(request.getDescription());
        permission.setAction(request.getAction());
        return permissionRepository.save(permission);
    }

    @Override
    public void delete(List<Long> ids) {
        List<Permission> permissions = permissionRepository.findAllById(ids);
        List<Long> deletedIds = permissions.stream()
                .filter(p -> p.getAction() != -1)
                .map(Permission::getId)
                .toList();
        if (deletedIds.isEmpty()) {
            throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
        }
        for (Permission permission : permissions) {
            permission.setAction(-1);
        }
        permissionRepository.saveAll(permissions);
    }

    @Override
    public Permission detail(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED));
        if (permission.getAction() == -1) {
            throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
        }
        return permission;
    }

    @Override
    public PermissionListResponse getList(PermissionListRequest request) {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getAction(),
                pageable
        );
        List<PermissionResponse> permissionResponses = getListPermission(permissionPage);
        PermissionListResponse response = new PermissionListResponse();
        response.setData(permissionResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(permissionPage.getSize());
        response.setTotalPages(permissionPage.getTotalPages());
        response.setTotalElements((int) permissionPage.getTotalElements());
        return response;
    }
}

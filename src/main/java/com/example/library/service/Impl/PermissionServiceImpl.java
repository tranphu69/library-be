package com.example.library.service.Impl;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Permission create(PermissionCreateRequest request) {
        if(permissionRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.PERMISSION_EXSITED);
        }
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        return permissionRepository.save(permission);
    }

    @Override
    public Permission update(PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXSITED));
        if(permissionRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.PERMISSION_EXSITED);
        }
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        return permissionRepository.save(permission);
    }

    @Override
    public PermissionListResponse getList(PermissionListRequest request) {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getStatus(),
                pageable
        );
        List<PermissionResponse> permissionResponses = permissionPage.getContent()
                .stream()
                .map(permission -> {
                    PermissionResponse response = new PermissionResponse();
                    response.setId(permission.getId());
                    response.setName(permission.getName());
                    response.setDescription(permission.getDescription());
                    response.setStatus(permission.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
        PermissionListResponse response = new PermissionListResponse();
        response.setData(permissionResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(permissionPage.getSize());
        response.setTotalPages(permissionPage.getTotalPages());
        response.setTotalElements((int) permissionPage.getTotalElements());
        return response;
    }

    private Sort createSort(String sortBy, String sortType) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "name";
        }
        if (!isValidSortField(sortBy)) {
            sortBy = "name";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortType)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    private boolean isValidSortField(String sortBy) {
        List<String> allowedFields = List.of("name", "status");
        return allowedFields.contains(sortBy);
    }

    @Override
    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }
}

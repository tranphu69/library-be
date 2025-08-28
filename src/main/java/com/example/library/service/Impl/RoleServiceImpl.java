package com.example.library.service.Impl;

import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleListRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.dto.response.role.RoleListResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Permission> permissions = permissionRepository.findAllActiveById(request.getPermissions());
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
        List<Permission> permissions = permissionRepository.findAllActiveById(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    public void delete(List<Long> ids) {
        List<Role> roles = roleRepository.findAllById(ids);
        if(roles.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        List<Long> deleteIds = roles.stream()
                .filter(p -> p.getStatus() == -1)
                .map(Role::getId)
                .toList();
        if(!deleteIds.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        for(Role role : roles){
            role.setStatus(-1);
        }
        roleRepository.saveAll(roles);
    }

    @Override
    public Role detail(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXSITED));
        if (role.getStatus() == -1) {
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        return role;
    }

    @Override
    public List<RoleResponse> getListAutoSearch(String keyword) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> rolePage = roleRepository.search(keyword, pageable);
        return rolePage.getContent()
                .stream()
                .map(role -> {
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    response.setStatus(role.getStatus());
                    response.setPermissions(role.getPermissions());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoleListResponse getList(RoleListRequest request) {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = convertToLongList(request.getPermissions());
        List<Permission> permissions = permissionRepository.findAllActiveById(arrNumber);
        if (permissions.size() != arrNumber.size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        Page<Role> rolePage = roleRepository.findRolesWithFilter(
                request.getName(),
                request.getStatus(),
                arrNumber,
                pageable
        );
        List<RoleResponse> roleResponses = getListRole(rolePage);
        RoleListResponse response = new RoleListResponse();
        response.setData(roleResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(rolePage.getSize());
        response.setTotalPages(rolePage.getTotalPages());
        response.setTotalElements((int) rolePage.getTotalElements());
        return response;
    }

    private List<Long> convertToLongList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private List<RoleResponse> getListRole(Page<Role> rolePage) {
        return rolePage.getContent()
                .stream()
                .map(role -> {
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    response.setStatus(role.getStatus());
                    Set<Permission> activePermissions = role.getPermissions()
                            .stream()
                            .filter(p -> p.getStatus() != -1)
                            .collect(Collectors.toSet());
                    response.setPermissions(activePermissions);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private boolean isValidSortField(String sortBy) {
        List<String> allowedFields = List.of("name", "status");
        return allowedFields.contains(sortBy);
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
}

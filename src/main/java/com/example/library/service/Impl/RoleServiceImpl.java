package com.example.library.service.Impl;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
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
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
}

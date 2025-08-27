package com.example.library.controller;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.entity.Role;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request){
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.create(request);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping
    public ApiResponse<RoleResponse> update(@Valid @RequestBody RoleUpdateRequest request) {
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.update(request);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }
}

package com.example.library.controller;

import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.service.PermissionService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permission")
public class PermissionController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PermissionService permissionService;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.create(request);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }
}

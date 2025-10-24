package com.example.library.controller;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.service.PermissionService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/{id}")
    public ApiResponse<PermissionResponse> update(@Valid @RequestBody PermissionRequest request, @PathVariable Long id) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.update(request, id);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("list");
        permissionService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Xóa permission thành công!");
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionResponse> detail(@PathVariable Long id) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.detail(id);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<PermissionListResponse> getList(
            @Valid @ModelAttribute PermissionListRequest request
    ){
        PermissionListResponse response = permissionService.getList(request);
        ApiResponse<PermissionListResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }
}

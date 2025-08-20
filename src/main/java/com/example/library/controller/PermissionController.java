package com.example.library.controller;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.service.PermissionService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionCreateRequest request) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.create(request);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<PermissionListResponse> getList(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "status") Integer status,
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size,
            @RequestParam(value = "sortBy") String sortBy,
            @RequestParam(value = "sortType") String sortType
    ){
        PermissionListRequest request = new PermissionListRequest();
        request.setName(name);
        request.setStatus(status);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortType(sortType);
        PermissionListResponse response = permissionService.getList(request);
        ApiResponse<PermissionListResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping
    public ApiResponse<PermissionResponse> update(@Valid @RequestBody PermissionUpdateRequest request) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.update(request);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable("id") Long id){
        permissionService.delete(id);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete permission successfull!");
        return apiResponse;
    }
}

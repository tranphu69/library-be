package com.example.library.controller;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("list");
        roleService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete roles successful!");
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getDetail(@PathVariable("id") Long id) {
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.detail(id);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/auto-search")
    public ApiResponse<List<RoleResponse>> getListAutoSearch(@RequestParam String keyword){
        List<RoleResponse> responses = roleService.getListAutoSearch(keyword);
        ApiResponse<List<RoleResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(responses);
        return apiResponse;
    }
}

package com.example.library.controller;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionCreateRequest request) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.create(request);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PostMapping("/import")
    public ApiResponse<?> importPermissions(@RequestParam("file") MultipartFile file) {
        permissionService.importFromExcel(file);
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Thành công");
        return apiResponse;
    }

    @PostMapping("/export")
    public void exportPermissions(@RequestBody PermissionListRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "permissions_list.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        permissionService.exportToExcel(request, response);
    }

    @GetMapping("/template-file")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "template_permissions.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        permissionService.exportTemplateExcel(response);
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

    @GetMapping("/auto-search")
    public ApiResponse<List<PermissionResponse>> getListAutoSearch(@RequestParam String keyword){
        List<PermissionResponse> response = permissionService.getListAutoSearch(keyword);
        ApiResponse<List<PermissionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionResponse> getDetail(@PathVariable("id") Long id) {
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        Permission permission = permissionService.detail(id);
        PermissionResponse response = modelMapper.map(permission, PermissionResponse.class);
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

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("list");
        permissionService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete permissions successful!");
        return apiResponse;
    }
}

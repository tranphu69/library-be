package com.example.library.controller;

import com.example.library.dto.request.Role.RoleListRequest;
import com.example.library.dto.request.Role.RoleRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.Role.RoleListResponse;
import com.example.library.dto.response.Role.RoleResponse;
import com.example.library.entity.Role;
import com.example.library.service.RoleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/role")
public class RoleController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.create(request);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(@Valid @RequestBody RoleRequest request, @PathVariable Long id) {
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.update(request, id);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("list");
        roleService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Xóa role thành công!");
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> detail(@PathVariable Long id) {
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        Role role = roleService.detail(id);
        RoleResponse response = modelMapper.map(role, RoleResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<RoleListResponse> getList(
            @Valid @ModelAttribute RoleListRequest request
    ) {
        RoleListResponse response = roleService.getList(request);
        ApiResponse<RoleListResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/template-file")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "template_roles.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        roleService.exportTemplateExcel(response);
    }

    @PostMapping("/export")
    public void exportRole(@RequestBody RoleListRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "roles_list.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        roleService.exportToExcel(request, response);
    }
}

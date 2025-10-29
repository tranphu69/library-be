package com.example.library.service;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Role.RoleListRequest;
import com.example.library.dto.request.Role.RoleRequest;
import com.example.library.dto.response.Role.RoleListResponse;
import com.example.library.entity.Role;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface RoleService {
    Role create(RoleRequest request);
    Role update(RoleRequest request, Long id);
    void delete(List<Long> id);
    Role detail(Long id);
    RoleListResponse getList(RoleListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(RoleListRequest request, HttpServletResponse response) throws IOException;
}

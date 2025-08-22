package com.example.library.service;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.entity.Permission;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PermissionService {
    Permission create(PermissionCreateRequest request);
    Permission update(PermissionUpdateRequest request);
    PermissionListResponse getList(PermissionListRequest request);
    void importFromExcel(MultipartFile file);
    void delete (List<Long> id);
    Permission detail(Long id);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(PermissionListRequest request, HttpServletResponse response) throws IOException;
}

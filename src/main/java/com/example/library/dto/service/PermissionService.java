package com.example.library.dto.service;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.entity.Permission;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PermissionService {
    Permission create(PermissionRequest request);
    Permission update(PermissionRequest request, Long id);
    void delete(List<Long> id);
    Permission detail(Long id);
    PageResponse<PermissionResponse> getList(PermissionListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(PermissionListRequest request, HttpServletResponse response) throws IOException;
    void importFromExcel(MultipartFile file);
    List<String> autoSearch(String keyword);
}

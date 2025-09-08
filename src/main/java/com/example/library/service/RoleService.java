package com.example.library.service;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleListRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.role.RoleListResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.dto.response.role.RoleResponseNoPermission;
import com.example.library.entity.Role;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RoleService {
    Role create(RoleCreateRequest request);
    Role update(RoleUpdateRequest request);
    void delete (List<Long> ids);
    Role detail(Long id);
    List<RoleResponseNoPermission> getListAutoSearch(String keyword);
    RoleListResponse getList(RoleListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(RoleListRequest request, HttpServletResponse response) throws IOException;
    void importFromExcel(MultipartFile file);
}

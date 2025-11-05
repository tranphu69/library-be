package com.example.library.service;

import com.example.library.dto.request.Role.RoleListRequest;
import com.example.library.dto.request.User.UserListRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.entity.User;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User create(UserRequest request);
    User update(UserRequest request, String id);
    void delete(List<String> id);
    User detail(String id);
    PageResponse<UserResponse> getList(UserListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
}

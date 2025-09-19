package com.example.library.service;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserListRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.user.UserListResponse;
import com.example.library.dto.response.user.UserResponseNoRole;
import com.example.library.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User create(UserCreateRequest request);
    User update(UserUpdateRequest request);
    void delete(List<Long> ids);
    User detail(Long id);
    List<UserResponseNoRole> getListAutoSearch(String keyword, String type);
    UserListResponse getList(UserListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(UserListRequest request, HttpServletResponse response) throws IOException;
    void importFromExcel(MultipartFile file);
}

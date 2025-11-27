package com.example.library.dto.service;

import com.example.library.dto.request.User.UserAutoSearch;
import com.example.library.dto.request.User.UserListRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.User.UserResponse;
import com.example.library.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User create(UserRequest request);
    User update(UserRequest request, String id);
    void delete(List<String> id);
    User detail(String id);
    PageResponse<UserResponse> getList(UserListRequest request);
    void exportTemplateExcel(HttpServletResponse response) throws IOException;
    void exportToExcel(UserListRequest request, HttpServletResponse response) throws IOException;
    void importFromExcel(MultipartFile file);
    List<String> autoSearch(UserAutoSearch keyword);
}

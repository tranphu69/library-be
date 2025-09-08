package com.example.library.service;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.user.UserResponse;
import com.example.library.dto.response.user.UserResponseNoRole;
import com.example.library.entity.User;

import java.util.List;

public interface UserService {
    User create(UserCreateRequest request);
    User update(UserUpdateRequest request);
    void delete(List<Long> ids);
    User detail(Long id);
    List<UserResponseNoRole> getListAutoSearch(String keyword, String type);
}

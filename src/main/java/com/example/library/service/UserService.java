package com.example.library.service;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.entity.User;

public interface UserService {
    User create(UserCreateRequest request);
    User update(UserUpdateRequest request);
}

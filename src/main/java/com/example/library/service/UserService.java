package com.example.library.service;

import com.example.library.dto.request.User.UserRequest;
import com.example.library.entity.User;

public interface UserService {
    User create(UserRequest request);
    User update(UserRequest request, String id);
}

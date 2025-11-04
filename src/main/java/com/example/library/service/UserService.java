package com.example.library.service;

import com.example.library.dto.request.User.UserRequest;
import com.example.library.entity.User;

import java.util.List;

public interface UserService {
    User create(UserRequest request);
    User update(UserRequest request, String id);
    void delete(List<String> id);
    User detail(String id);
}

package com.example.library.controller;

import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.User.UserResponse;
import com.example.library.entity.User;
import com.example.library.service.UserService;
import com.example.library.validation.OnCreate;
import com.example.library.validation.OnUpdate;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> create(@Validated(OnCreate.class) @RequestBody UserRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.create(request);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@Validated(OnUpdate.class) @RequestBody UserRequest request, @PathVariable String id) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.update(request, id);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }
}

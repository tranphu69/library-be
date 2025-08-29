package com.example.library.controller;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.user.UserResponse;
import com.example.library.entity.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.create(request);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }
}

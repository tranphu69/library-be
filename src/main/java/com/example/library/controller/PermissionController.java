package com.example.library.controller;

import com.example.library.dto.response.ApiResponse;
import com.example.library.service.PermissionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permission")
public class PermissionController {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PermissionService permissionService;


}

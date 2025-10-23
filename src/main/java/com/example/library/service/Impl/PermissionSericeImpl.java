package com.example.library.service.Impl;

import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionSericeImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;
}

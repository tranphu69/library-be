package com.example.library.service.Impl;

import com.example.library.dto.request.User.UserRequest;
import com.example.library.entity.User;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public User create(UserRequest request) {
        String newUsername = request.getUsername().trim();
        String newEmail = request.getEmail().trim();
        return null;
    }
}

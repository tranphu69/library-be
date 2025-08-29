package com.example.library.service.Impl;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User create(UserCreateRequest request) {
        if(userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new AppException(ErrorCode.EMAIL_EXSITED);
        }
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXSITED);
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setIsActive(request.getIsActive());
        List<Role> roles;
        if(request.getRoles().isEmpty()){
            roles = roleRepository.findByName("USER");
            if (roles.isEmpty()) {
                throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
            } else {
                user.setRoles(new HashSet<>(roles));
            }
        } else {
            roles = roleRepository.findAllActiveById(request.getRoles());
            if (roles.size() != request.getRoles().size()){
                throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
            }
            user.setRoles(new HashSet<>(roles));
        }
        return userRepository.save(user);
    }
}

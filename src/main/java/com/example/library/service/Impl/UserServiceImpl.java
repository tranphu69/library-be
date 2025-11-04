package com.example.library.service.Impl;

import com.example.library.dto.request.User.UserRequest;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.messageError.RoleErrorCode;
import com.example.library.exception.messageError.UserErrorCode;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public User create(UserRequest request) {
        String newUsername = request.getUsername().trim();
        if (userRepository.existsByUsername(newUsername)) {
            throw new AppException(UserErrorCode.USER_USERNAME_EXSITED);
        }
        String newEmail = request.getEmail().trim();
        if (userRepository.existsByEmail(newEmail)) {
            throw new AppException(UserErrorCode.USER_EMAIL_EXSITED);
        }
        List<Role> roles = new ArrayList<>();
        if (!request.getRoles().isEmpty()) {
            roles = roleRepository.findAllActiveById(request.getRoles());
            if (roles.size() != request.getRoles().size()) {
                throw new AppException(RoleErrorCode.ROLE_NO_EXSITED);
            }
        }
        User user = new User();
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        user.setCode(request.getCode() != null ? request.getCode().trim() : null);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setMajor(request.getMajor() != null ? request.getMajor().trim() : null);
        user.setCourse(request.getCourse() != null ? request.getCourse().trim() : null);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPosition(request.getPosition());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setStatus(request.getStatus());
        user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }

    @Override
    public User update(UserRequest request, String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        String newUsername = request.getUsername().trim();
        if (userRepository.existsByUsernameAndIdNot(newUsername, id)) {
            throw new AppException(UserErrorCode.USER_USERNAME_EXSITED);
        }
        String newEmail = request.getEmail().trim();
        if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
            throw new AppException(UserErrorCode.USER_EMAIL_EXSITED);
        }
        List<Role> roles = new ArrayList<>();
        if (!request.getRoles().isEmpty()) {
            roles = roleRepository.findAllActiveById(request.getRoles());
            if (roles.size() != request.getRoles().size()) {
                throw new AppException(RoleErrorCode.ROLE_NO_EXSITED);
            }
        }
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        user.setCode(request.getCode() != null ? request.getCode().trim() : null);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setMajor(request.getMajor() != null ? request.getMajor().trim() : null);
        user.setCourse(request.getCourse() != null ? request.getCourse().trim() : null);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPosition(request.getPosition());
        user.setGender(request.getGender());
        user.setDob(request.getDob());
        user.setStatus(request.getStatus());
        user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }
}

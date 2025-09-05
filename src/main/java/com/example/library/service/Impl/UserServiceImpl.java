package com.example.library.service.Impl;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.role.RoleResponseNoPermission;
import com.example.library.dto.response.user.UserResponse;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public User update(UserUpdateRequest request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXSITED));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), request.getId())) {
            throw new AppException(ErrorCode.EMAIL_EXSITED);
        }
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), request.getId())) {
            throw new AppException(ErrorCode.USERNAME_EXSITED);
        }
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
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

    @Override
    public void delete(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        if(users.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_EXSITED);
        }
        List<Long> deleteIds = users.stream()
                .filter(p -> p.getIsActive() == -1)
                .map(User::getId)
                .toList();
        if(!deleteIds.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_EXSITED);
        }
        for (User user : users) {
            user.setIsActive(-1);
        }
        userRepository.saveAll(users);
    }

    @Override
    public User detail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXSITED));
        if(user.getIsActive() == -1){
            throw new AppException(ErrorCode.USER_NOT_EXSITED);
        }
        Set<Role> activeRole = user.getRoles()
                .stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toSet());
        user.setRoles(activeRole);
        return user;
    }
}

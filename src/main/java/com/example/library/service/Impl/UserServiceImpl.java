package com.example.library.service.Impl;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserListRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.dto.response.role.RoleResponseNoPermission;
import com.example.library.dto.response.user.UserListResponse;
import com.example.library.dto.response.user.UserResponse;
import com.example.library.dto.response.user.UserResponseNoRole;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import com.example.library.utils.Utils;
import jakarta.validation.Validator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private ModelMapper modelMapper;

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

    @Override
    public List<UserResponseNoRole> getListAutoSearch(String keyword, String type) {
        List<User> users = List.of();
        String normalizedQuery = keyword.trim();
        if (Objects.equals(type, "email")) {
            users = userRepository.findByEmailStartingWithIgnoreCase(normalizedQuery);
        } else if (Objects.equals(type, "username")) {
            users = userRepository.findByUsernameStartingWithIgnoreCase(normalizedQuery);
        }
        return users.stream()
                .map(user -> {
                    UserResponseNoRole response = new UserResponseNoRole();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserListResponse getList(UserListRequest request) {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = Utils.convertToLongList(request.getRoles());
        List<Role> roles = roleRepository.findAllActiveById(arrNumber);
        if (roles.size() != arrNumber.size()) {
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        Page<User> userPage = userRepository.findUsersWithFilter(
                request.getEmail(),
                request.getIsActive(),
                request.getUsername(),
                arrNumber,
                pageable
        );
        List<UserResponse> userResponses = getListUser(userPage);
        UserListResponse response = new UserListResponse();
        response.setData(userResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(userPage.getSize());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalElements((int) userPage.getTotalElements());
        return response;
    }

    private List<UserResponse> getListUser(Page<User> userPage) {
        return userPage.getContent()
                .stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setIsActive(user.getIsActive());
                    response.setCreatedAt(user.getCreatedAt());
                    response.setUpdatedAt(user.getUpdatedAt());
                    Set<Role> activeRoles = user.getRoles()
                            .stream()
                            .filter(p -> p.getStatus() != -1 && p.getStatus() != 0)
                            .collect(Collectors.toSet());
                    Set<RoleResponseNoPermission> roleResponses = activeRoles.stream()
                            .map(role -> modelMapper.map(role, RoleResponseNoPermission.class))
                            .collect(Collectors.toSet());
                    response.setRoles(roleResponses);
                    return response;
                })
                .collect(Collectors.toList());
    }
}

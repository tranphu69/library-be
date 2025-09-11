package com.example.library.controller;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserListRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.user.UserListResponse;
import com.example.library.dto.response.user.UserResponse;
import com.example.library.dto.response.user.UserResponseNoRole;
import com.example.library.entity.User;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @PutMapping
    public ApiResponse<UserResponse> update(@Valid @RequestBody UserUpdateRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.update(request);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("list");
        userService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete users successful!");
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getDetail(@PathVariable("id") Long id) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.detail(id);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @GetMapping("/auto-search")
    public ApiResponse<List<UserResponseNoRole>> getListAutoSearch(
            @RequestParam("keyword") String keyword,
            @RequestParam("type") String type
    ) {
        if(!"email".equalsIgnoreCase(type) && !"username".equalsIgnoreCase(type)) {
            throw new AppException(ErrorCode.ERROR_TYPE);
        }
        List<UserResponseNoRole> results = userService.getListAutoSearch(keyword, type);
        ApiResponse<List<UserResponseNoRole>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(results);
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<UserListResponse> getList(
            @Valid @ModelAttribute UserListRequest request
    ){
       UserListResponse response = userService.getList(request);
       ApiResponse<UserListResponse> apiResponse = new ApiResponse<>();
       apiResponse.setResult(response);
       return apiResponse;
    }

    @GetMapping("/template-file")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "template_users.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        userService.exportTemplateExcel(response);
    }
}

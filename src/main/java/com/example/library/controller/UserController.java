package com.example.library.controller;

import com.example.library.dto.request.User.UserAutoSearch;
import com.example.library.dto.request.User.UserListRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.User.UserResponse;
import com.example.library.entity.User;
import com.example.library.dto.service.UserService;
import com.example.library.validation.OnCreate;
import com.example.library.validation.OnUpdate;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/delete")
    public ApiResponse<String> delete(@RequestBody Map<String, List<String>> request) {
        List<String> ids = request.get("list");
        userService.delete(ids);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Xóa người dùng thành công!");
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> detail(@PathVariable String id) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        User user = userService.detail(id);
        UserResponse response = modelMapper.map(user, UserResponse.class);
        apiResponse.setResult(response);
        return apiResponse;
    }

    @PostMapping("/list")
    public ApiResponse<PageResponse<UserResponse>> getList(@Valid @RequestBody UserListRequest request) {
        PageResponse<UserResponse> response = userService.getList(request);
        ApiResponse<PageResponse<UserResponse>> apiResponse = new ApiResponse<>();
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

    @PostMapping("/export")
    public void exportRole(@RequestBody UserListRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "users_list.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        userService.exportToExcel(request, response);
    }

    @PostMapping("/import")
    public ApiResponse<?> importUsers(@RequestParam("file")MultipartFile file) {
        userService.importFromExcel(file);
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Nhập thành công!");
        return apiResponse;
    }

    @PostMapping("/auto-search")
    public ApiResponse<List<String>> autoSearch(@RequestBody UserAutoSearch keyword){
        List<String> autoList = userService.autoSearch(keyword);
        ApiResponse<List<String>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(autoList);
        return apiResponse;
    }
}

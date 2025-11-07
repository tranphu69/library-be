package com.example.library.service.Impl;

import com.example.library.dto.request.User.UserAutoSearch;
import com.example.library.dto.request.User.UserListRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.NoAction;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import com.example.library.enums.Position;
import com.example.library.exception.AppException;
import com.example.library.enums.AccountStatus;
import com.example.library.enums.Gender;
import com.example.library.exception.messageError.RoleErrorCode;
import com.example.library.exception.messageError.UserErrorCode;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import com.example.library.utils.Utils;
import com.example.library.utils.UtilsExcel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EntityManager entityManager;

    private List<UserResponse> getListUser(Page<User> userPage) {
        return userPage.getContent()
                .stream()
                .map(user -> {
                    Set<NoAction> noActionSet = user.getRoles().stream()
                            .map(r -> {
                                NoAction dto = new NoAction();
                                dto.setId(r.getId());
                                dto.setName(r.getName());
                                dto.setDescription(r.getDescription());
                                return dto;
                            })
                            .collect(Collectors.toSet());
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setEmail(user.getEmail());
                    response.setFullName(user.getFullName());
                    response.setCode(user.getCode());
                    response.setPhone(user.getPhone());
                    response.setAvatarUrl(user.getAvatarUrl());
                    response.setMajor(user.getMajor());
                    response.setCourse(user.getCourse());
                    response.setPosition(user.getPosition());
                    response.setGender(user.getGender());
                    response.setDob(user.getDob());
                    response.setStatus(user.getStatus());
                    response.setCreatedAt(user.getCreatedAt());
                    response.setUpdatedAt(user.getUpdatedAt());
                    response.setCreatedBy(user.getCreatedBy());
                    response.setUpdatedBy(user.getUpdatedBy());
                    response.setLastLoginAt(user.getLastLoginAt());
                    response.setFailedLoginAttempts(user.getFailedLoginAttempts());
                    response.setLockedUntil(user.getLockedUntil());
                    response.setTwoFactorEnabled(user.getTwoFactorEnabled());
                    response.setRoles(noActionSet);
                    return response;
                })
                .collect(Collectors.toList());
    }

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
        String newPhone = request.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            newPhone = newPhone.trim();
            if (userRepository.existsByPhone(newPhone)) {
                throw new AppException(UserErrorCode.USER_PHONE_EXSITED);
            }
        }
        String newCode = request.getCode();
        if (newCode != null && !newCode.trim().isEmpty()) {
            newCode = newCode.trim();
            if (userRepository.existsByCode(newCode)) {
                throw new AppException(UserErrorCode.USER_CODE_EXSITED);
            }
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
        user.setPhone(newPhone);
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
    @Transactional
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
        String newPhone = request.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            newPhone = newPhone.trim();
            if (userRepository.existsByPhoneAndIdNot(newPhone, id)) {
                throw new AppException(UserErrorCode.USER_PHONE_EXSITED);
            }
        }
        String newCode = request.getCode();
        if (newCode != null && !newCode.trim().isEmpty()) {
            newCode = newCode.trim();
            if (userRepository.existsByCodeAndIdNot(newCode, id)) {
                throw new AppException(UserErrorCode.USER_CODE_EXSITED);
            }
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
        user.setPhone(newPhone);
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
    @Transactional
    public void delete(List<String> id) {
        List<User> users = userRepository.findAllById(id);
        List<String> deleteIds = users.stream()
                .filter(u -> u.getStatus() != AccountStatus.DELETED)
                .map(User::getId)
                .toList();
        if (deleteIds.isEmpty()) {
            throw new AppException(UserErrorCode.USER_NO_EXSITED);
        }
        for (User user : users) {
            user.setStatus(AccountStatus.DELETED);
        }
        userRepository.saveAll(users);
    }

    @Override
    public User detail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        if (user.getStatus() == AccountStatus.DELETED) {
            throw new AppException(UserErrorCode.USER_NO_EXSITED);
        }
        return user;
    }

    @Override
    public PageResponse<UserResponse> getList(UserListRequest request) {
        List<Long> roleIds = Arrays.stream(request.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("username", "email", "fullName", "code", "phone", "major", "course", "position", "gender",
                "dob", "status", "createdAt", "updatedAt", "createdBy", "updatedBy", "lastLoginAt", "failedLoginAttempts", "lockedUntil", "twoFactorEnabled"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<User> userPage = userRepository.findUsersWithAllRoles(
                request.getUsername(),
                request.getEmail(),
                request.getFullName(),
                request.getCode(),
                request.getPhone(),
                request.getMajor(),
                request.getCourse(),
                request.getPosition(),
                request.getGender(),
                request.getDob(),
                request.getStatus(),
                roleIds,
                pageable
        );
        List<UserResponse> userResponses = getListUser(userPage);
        PageResponse<UserResponse> response = new PageResponse<>();
        response.setData(userResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(request.getSize());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalElements((int) userPage.getTotalElements());
        return response;
    }

    @Override
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        List<UserResponse> userResponses = new ArrayList<>();
        List<String> headers = List.of(
                "username * \n(Tối đa 100 kí tự)",
                "email * \n(Tối đa 100 kí tự, nhập đúng định dạng email)",
                "password * \n(Ít nhất 10 và tối đa 16 kí tự, mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt)",
                "fullname \n(Tối đa 100 kí tự)",
                "code \n(Tối đa 100 kí tự)",
                "phone \n(Tối đa 100 kí tự, chỉ nhập số)",
                "major \n(Tối đa 100 kí tự)",
                "course \n(Tối đa 100 kí tự)",
                "position \n(Tối đa 100 kí tự)",
                "gender \n(Tối đa 100 kí tự)",
                "dob",
                "status * \n(Chọn ít nhất 1 trạng thái)",
                "twoFactorEnabled",
                "roles * \n(Phải chọn ít nhất 1 role)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                8, new String[]{"STUDENT", "LECTURER", "LIBRARIAN", "ADMIN"},
                9, new String[]{"MALE", "FEMALE"},
                11, new String[]{"ACTIVE", "SUSPENDED", "BANNED", "DELETED"}
        );
        Map<Integer, Integer> widths = Map.ofEntries(
                Map.entry(0, 7000),
                Map.entry(1, 9000),
                Map.entry(2, 14000),
                Map.entry(3, 7000),
                Map.entry(4, 7000),
                Map.entry(5, 8000),
                Map.entry(6, 7000),
                Map.entry(7, 7000),
                Map.entry(8, 7000),
                Map.entry(9, 7000),
                Map.entry(10, 7000),
                Map.entry(11, 7000),
                Map.entry(12, 7000),
                Map.entry(13, 7000)
        );
        List<String> headers1 = List.of(
                "Tên * \n(Tối đa 50 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"
        );
        Map<Integer, String[]> dropdowns1 = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths1 = Map.of(
                0, 8000,
                1, 12000,
                2, 10000
        );
        List<RoleResponse> roleResponses = roleRepository.findAllStatus1()
                .stream().map(r -> {
                    RoleResponse roleResponse = new RoleResponse();
                    roleResponse.setName(r.getName());
                    roleResponse.setDescription(r.getDescription());
                    roleResponse.setAction(r.getAction());
                    return roleResponse;
                }).toList();
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH USER",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        userResponses,
                        u -> List.of(
                                u.getUsername(),
                                u.getEmail(),
                                "",
                                u.getFullName(),
                                u.getCode(),
                                u.getPhone(),
                                u.getMajor(),
                                u.getCourse(),
                                u.getPosition(),
                                u.getGender(),
                                u.getDob(),
                                u.getStatus(),
                                u.getTwoFactorEnabled(),
                                u.getRoles().stream().map(NoAction::getName).collect(Collectors.joining(", "))
                        ),
                        (workbook, cells) -> {
                            Font boldFont = workbook.createFont();
                            boldFont.setBold(true);
                            Font redBoldFont = workbook.createFont();
                            redBoldFont.setBold(true);
                            redBoldFont.setColor(IndexedColors.RED.getIndex());
                            Font italicFont = workbook.createFont();
                            italicFont.setItalic(true);
                            for (int i = 0; i < headers.size(); i++) {
                                String header = headers.get(i);
                                XSSFRichTextString richText = new XSSFRichTextString(header);
                                switch (i) {
                                    case 0, 2 -> {
                                        richText.applyFont(0, 8, boldFont);
                                        richText.applyFont(8, 10, redBoldFont);
                                        richText.applyFont(10, header.length(), italicFont);
                                    }
                                    case 1, 13 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(5, 7, redBoldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 11 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(6, 8, redBoldFont);
                                        richText.applyFont(9, header.length(), italicFont);
                                    }
                                    case 3, 8 -> {
                                        richText.applyFont(0, 8, boldFont);
                                        richText.applyFont(9, header.length(), italicFont);
                                    }
                                    case 4 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(5, header.length(), italicFont);
                                    }
                                    case 5, 6 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 7, 9 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 10, 12 -> {
                                        richText.applyFont(boldFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH ROLE",
                        "Danh sách role",
                        headers1,
                        dropdowns1,
                        widths1,
                        roleResponses,
                        r -> List.of(
                                r.getName(),
                                r.getDescription(),
                                r.getAction() == 1 ? "Hoạt động" : "Không hoạt động"
                        ),
                        (workbook, cells) -> {
                            Font boldFont = workbook.createFont();
                            boldFont.setBold(true);
                            Font redBoldFont = workbook.createFont();
                            redBoldFont.setBold(true);
                            redBoldFont.setColor(IndexedColors.RED.getIndex());
                            Font italicFont = workbook.createFont();
                            italicFont.setItalic(true);
                            for (int i = 0; i < headers1.size(); i++) {
                                String header = headers1.get(i);
                                XSSFRichTextString richText = new XSSFRichTextString(header);
                                switch (i) {
                                    case 0 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(4, 5, redBoldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 1 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 2 -> {
                                        richText.applyFont(0, 11, boldFont);
                                        richText.applyFont(11, 13, redBoldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }

                )
        );
        UtilsExcel.exportToExcel(
                response,
                sheets
        );
    }

    @Override
    public void exportToExcel(UserListRequest request, HttpServletResponse response) throws IOException {
        List<Long> roleIds = Arrays.stream(request.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("username", "email", "fullName", "code", "phone", "major", "course", "position", "gender",
                "dob", "status", "createdAt", "updatedAt", "createdBy", "updatedBy", "lastLoginAt", "failedLoginAttempts", "lockedUntil", "twoFactorEnabled"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<User> userPage = userRepository.findUsersWithAllRoles(
                request.getUsername(),
                request.getEmail(),
                request.getFullName(),
                request.getCode(),
                request.getPhone(),
                request.getMajor(),
                request.getCourse(),
                request.getPosition(),
                request.getGender(),
                request.getDob(),
                request.getStatus(),
                roleIds,
                pageable
        );
        List<UserResponse> userResponses = getListUser(userPage);
        List<String> headers = List.of(
                "username * \n(Tối đa 100 kí tự)",
                "email * \n(Tối đa 100 kí tự, nhập đúng định dạng email)",
                "fullname \n(Tối đa 100 kí tự)",
                "code \n(Tối đa 100 kí tự)",
                "phone \n(Tối đa 100 kí tự, chỉ nhập số)",
                "major \n(Tối đa 100 kí tự)",
                "course \n(Tối đa 100 kí tự)",
                "position \n(Tối đa 100 kí tự)",
                "gender \n(Tối đa 100 kí tự)",
                "dob",
                "status * \n(Chọn ít nhất 1 trạng thái)",
                "twoFactorEnabled",
                "roles * \n(Phải chọn ít nhất 1 role)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                7, new String[]{"STUDENT", "LECTURER", "LIBRARIAN", "ADMIN"},
                8, new String[]{"MALE", "FEMALE"},
                10, new String[]{"ACTIVE", "SUSPENDED", "BANNED", "DELETED"}
        );
        Map<Integer, Integer> widths = Map.ofEntries(
                Map.entry(0, 7000),
                Map.entry(1, 12000),
                Map.entry(2, 7000),
                Map.entry(3, 7000),
                Map.entry(4, 7000),
                Map.entry(5, 9000),
                Map.entry(6, 7000),
                Map.entry(7, 7000),
                Map.entry(8, 7000),
                Map.entry(9, 7000),
                Map.entry(10, 7000),
                Map.entry(11, 7000),
                Map.entry(12, 7000)
        );
        List<String> headers1 = List.of(
                "Tên * \n(Tối đa 50 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"
        );
        Map<Integer, String[]> dropdowns1 = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths1 = Map.of(
                0, 8000,
                1, 12000,
                2, 10000
        );
        List<RoleResponse> roleResponses = roleRepository.findAllStatus1()
                .stream().map(r -> {
                    RoleResponse roleResponse = new RoleResponse();
                    roleResponse.setName(r.getName());
                    roleResponse.setDescription(r.getDescription());
                    roleResponse.setAction(r.getAction());
                    return roleResponse;
                }).toList();
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH USER",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        userResponses,
                        u -> List.of(
                                u.getUsername(),
                                u.getEmail(),
                                u.getFullName() != null ? u.getFullName() : "",
                                u.getCode() != null ? u.getCode() : "",
                                u.getPhone() != null ? u.getPhone() : "",
                                u.getMajor() != null ? u.getMajor() : "",
                                u.getCourse() != null ? u.getCourse() : "",
                                u.getPosition() != null ? u.getPosition() : "",
                                u.getGender() != null ? u.getGender() : "",
                                u.getDob() != null ? u.getDob() : "",
                                u.getStatus() != null ? u.getStatus() : "",
                                u.getTwoFactorEnabled() != null ? u.getTwoFactorEnabled() : false,
                                u.getRoles().stream().map(NoAction::getName).collect(Collectors.joining(", "))
                        ),
                        (workbook, cells) -> {
                            Font boldFont = workbook.createFont();
                            boldFont.setBold(true);
                            Font redBoldFont = workbook.createFont();
                            redBoldFont.setBold(true);
                            redBoldFont.setColor(IndexedColors.RED.getIndex());
                            Font italicFont = workbook.createFont();
                            italicFont.setItalic(true);
                            for (int i = 0; i < headers.size(); i++) {
                                String header = headers.get(i);
                                XSSFRichTextString richText = new XSSFRichTextString(header);
                                switch (i) {
                                    case 0 -> {
                                        richText.applyFont(0, 8, boldFont);
                                        richText.applyFont(8, 10, redBoldFont);
                                        richText.applyFont(10, header.length(), italicFont);
                                    }
                                    case 1, 12 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(5, 7, redBoldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 10 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(6, 8, redBoldFont);
                                        richText.applyFont(9, header.length(), italicFont);
                                    }
                                    case 2, 7 -> {
                                        richText.applyFont(0, 8, boldFont);
                                        richText.applyFont(9, header.length(), italicFont);
                                    }
                                    case 3 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(5, header.length(), italicFont);
                                    }
                                    case 4, 5 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 6, 8 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 9, 11 -> {
                                        richText.applyFont(boldFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH ROLE",
                        "Danh sách role",
                        headers1,
                        dropdowns1,
                        widths1,
                        roleResponses,
                        r -> List.of(
                                r.getName(),
                                r.getDescription(),
                                r.getAction() == 1 ? "Hoạt động" : "Không hoạt động"
                        ),
                        (workbook, cells) -> {
                            Font boldFont = workbook.createFont();
                            boldFont.setBold(true);
                            Font redBoldFont = workbook.createFont();
                            redBoldFont.setBold(true);
                            redBoldFont.setColor(IndexedColors.RED.getIndex());
                            Font italicFont = workbook.createFont();
                            italicFont.setItalic(true);
                            for (int i = 0; i < headers1.size(); i++) {
                                String header = headers1.get(i);
                                XSSFRichTextString richText = new XSSFRichTextString(header);
                                switch (i) {
                                    case 0 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(4, 5, redBoldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 1 -> {
                                        richText.applyFont(0, 6, boldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 2 -> {
                                        richText.applyFont(0, 11, boldFont);
                                        richText.applyFont(11, 13, redBoldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }

                )
        );
        UtilsExcel.exportToExcel(
                response,
                sheets
        );
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Dùng BigDecimal để giữ nguyên số (tránh mất chữ số)
                return new java.math.BigDecimal(cell.getNumericCellValue())
                        .toPlainString();
            default:
                return "";
        }
    }

    @Override
    @Transactional
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Set<String> existingUsernames = new HashSet<>(userRepository.findAllUsernames());
            Set<String> excelUsernames = new HashSet<>();
            Set<String> existingEmails = new HashSet<>(userRepository.findAllEmails());
            Set<String> excelEmails = new HashSet<>();
            Set<String> existingPhones = new HashSet<>(userRepository.findAllPhones());
            Set<String> excelPhones = new HashSet<>();
            Set<String> existingCodes = new HashSet<>(userRepository.findAllCodes());
            Set<String> excelCodes = new HashSet<>();
            List<User> users = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();
            while (lastRow >= 0 && (sheet.getRow(lastRow) == null || UtilsExcel.isRowEmpty(sheet.getRow(lastRow)))) {
                lastRow--;
            }
            for(int i = 4; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if(UtilsExcel.isRowEmpty(row)) continue;
                UserRequest request = new UserRequest();
                request.setUsername(UtilsExcel.getCellValue(row.getCell(0)));
                request.setEmail(UtilsExcel.getCellValue(row.getCell(1)));
                request.setPassword(UtilsExcel.getCellValue(row.getCell(2)));
                request.setFullName(UtilsExcel.getCellValue(row.getCell(3)));
                request.setCode(UtilsExcel.getCellValue(row.getCell(4)));
                request.setPhone(getCellValue(row.getCell(5)));
                request.setMajor(UtilsExcel.getCellValue(row.getCell(6)));
                request.setCourse(UtilsExcel.getCellValue(row.getCell(7)));
                Position newPosition = UtilsExcel.getCellValue(row.getCell(8)) != null && !UtilsExcel.getCellValue(row.getCell(8)).isEmpty() ? Position.valueOf(UtilsExcel.getCellValue(row.getCell(8)).trim()) : null;
                request.setPosition(newPosition);
                Gender newGender = UtilsExcel.getCellValue(row.getCell(9)) != null && !UtilsExcel.getCellValue(row.getCell(9)).isEmpty() ? Gender.valueOf(UtilsExcel.getCellValue(row.getCell(9)).trim()) : null;
                request.setGender(newGender);
                Cell cell = row.getCell(10);
                LocalDate newDate = null;
                if (cell != null) {
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        newDate = cell.getLocalDateTimeCellValue().toLocalDate();
                    } else {
                        String raw = UtilsExcel.getCellValue(cell);
                        if (raw != null && !raw.trim().isEmpty()) {
                            newDate = LocalDate.parse(raw.trim());
                        }
                    }
                }
                request.setDob(newDate);
                AccountStatus newStatus = UtilsExcel.getCellValue(row.getCell(11)) != null && !UtilsExcel.getCellValue(row.getCell(11)).isEmpty() ? AccountStatus.valueOf(UtilsExcel.getCellValue(row.getCell(11)).trim()) : null;
                request.setStatus(newStatus);
                Boolean newTwoFactorEnabled = UtilsExcel.getCellValue(row.getCell(12)) != null && !UtilsExcel.getCellValue(row.getCell(12)).trim().isEmpty() ? Boolean.valueOf(UtilsExcel.getCellValue(row.getCell(12)).trim()) : false;
                request.setTwoFactorEnabled(newTwoFactorEnabled);
                String roleStr = UtilsExcel.getCellValue(row.getCell(13));
                List<String> roleArr = Arrays.stream(roleStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                Set<Role> roles = roleRepository.findByNameIn(roleArr);
                List<Long> changeRoles = roles.stream()
                        .map(Role::getId)
                        .toList();
                request.setRoles(changeRoles);
                Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (!excelUsernames.add(request.getUsername())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (existingUsernames.contains(request.getUsername())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (!excelEmails.add(request.getEmail())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (existingEmails.contains(request.getEmail())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (!excelCodes.add(request.getCode())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (existingCodes.contains(request.getCode())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (!excelPhones.add(request.getPhone())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                if (existingPhones.contains(request.getPhone())) {
                    throw new AppException(UserErrorCode.USER_ERROR_FILE);
                }
                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(request.getPassword());
                user.setFullName(!request.getFullName().isEmpty() ? request.getFullName().trim() : null);
                user.setCode(!request.getCode().isEmpty() ? request.getCode().trim() : null);
                user.setPhone(!request.getPhone().isEmpty() ? request.getPhone() : null);
                user.setMajor(!request.getMajor().isEmpty() ? request.getMajor().trim() : null);
                user.setCourse(!request.getCourse().isEmpty() ? request.getCourse().trim() : null);
                user.setAvatarUrl(request.getAvatarUrl());
                user.setPosition(request.getPosition());
                user.setGender(request.getGender());
                user.setDob(request.getDob());
                user.setStatus(request.getStatus());
                user.setTwoFactorEnabled(Boolean.valueOf(request.getTwoFactorEnabled()));
                user.setRoles(new HashSet<>(roles));
                users.add(user);
            }
            userRepository.saveAll(users);
            workbook.close();
        } catch (IOException e) {
            throw new AppException(UserErrorCode.USER_NOT_READ_FILE);
        }
    }

    @Override
    public List autoSearch(UserAutoSearch keyword) {
        if (keyword.getKeyword() == null || keyword.getKeyword().trim().isEmpty() || keyword.getType() == null || keyword.getType().trim().isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> columnMapping = Map.of(
                "username", "username",
                "email", "email",
                "fullName", "full_name",
                "code", "code",
                "phone", "phone",
                "major", "major",
                "course", "course"
        );
        String type = keyword.getType().trim();
        if (!columnMapping.containsKey(type)) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        String column = columnMapping.get(type);
        String sql = "SELECT DISTINCT " + column +
                " FROM users WHERE LOWER(" + column + ") LIKE LOWER(CONCAT('%', :keyword, '%'))";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("keyword", keyword.getKeyword().trim());
        return query.getResultList();
    }
}

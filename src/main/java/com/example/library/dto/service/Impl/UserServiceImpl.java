package com.example.library.dto.service.Impl;

import com.example.library.dto.request.User.UserAutoSearch;
import com.example.library.dto.request.User.UserListRequest;
import com.example.library.dto.request.User.UserRequest;
import com.example.library.dto.response.NoAction;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.dto.response.User.UserResponse;
import com.example.library.dto.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EntityManager entityManager;
    private final List<String> headers1 = List.of(
            "Tên * \n(Tối đa 50 kí tự)",
            "Mô tả \n(Tối đa 255 kí tự)",
            "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"
    );
    private final Map<Integer, Integer> widths1 = Map.of(
            0, 8000,
            1, 12000,
            2, 10000
    );

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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

    private User getUser(UserRequest request, Set<Role> roles) {
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        user.setTwoFactorEnabled(request.getTwoFactorEnabled());
        user.setCreatedBy(currentUsername);
        user.setRoles(new HashSet<>(roles));
        return user;
    }

    private String getString(UserAutoSearch keyword) {
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
            throw new IllegalArgumentException("Loại không hợp lệ: " + type);
        }
        return columnMapping.get(type);
    }

    private UtilsExcel.ExcelSheetConfig<?> buildRoleSheets() {
        List<RoleResponse> roleResponses = roleRepository.findAllStatus1()
                .stream().map(r -> {
                    RoleResponse roleResponse = new RoleResponse();
                    roleResponse.setName(r.getName());
                    roleResponse.setDescription(r.getDescription());
                    roleResponse.setAction(r.getAction());
                    return roleResponse;
                }).toList();
        return new UtilsExcel.ExcelSheetConfig<> (
                "DANH SÁCH VAI TRÒ",
                "Danh sách vai trò",
                headers1,
                null,
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
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USER_CREATE')")
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
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = new User();
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        user.setCreatedBy(currentUsername);
        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USER_UPDATE')")
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
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
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
        user.setUpdatedBy(currentUsername);
        user.setRoles(new HashSet<>(roles));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USER_DELETE')")
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
    @PreAuthorize("hasAuthority('USER_SEARCH')")
    public User detail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NO_EXSITED));
        if (user.getStatus() == AccountStatus.DELETED) {
            throw new AppException(UserErrorCode.USER_NO_EXSITED);
        }
        return user;
    }

    @Override
    @PreAuthorize("hasAuthority('USER_SEARCH')")
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
    @PreAuthorize("hasAuthority('USER_SEARCH')")
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        List<UserResponse> userResponses = new ArrayList<>();
        List<String> headers = List.of(
                "Tên người dùng * \n(Tối đa 100 kí tự)",
                "Email * \n(Tối đa 100 kí tự, nhập đúng định dạng email)",
                "Mật khẩu * \n(Ít nhất 10 và tối đa 16 kí tự, mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt)",
                "Tên đầy đủ \n(Tối đa 100 kí tự)",
                "Mã \n(Tối đa 100 kí tự)",
                "Số điện thoại \n(Tối đa 100 kí tự, chỉ nhập số)",
                "Ngành \n(Tối đa 100 kí tự)",
                "Khóa \n(Tối đa 100 kí tự)",
                "Vai trò của người dùng \n(Tối đa 100 kí tự)",
                "Giới tính \n(Tối đa 100 kí tự)",
                "Ngày sinh",
                "Trạng thái * \n(Chọn ít nhất 1 trạng thái)",
                "Xác thực hai lớp",
                "Vai trò * \n(Phải chọn ít nhất 1 Vai trò)"
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
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH NGƯỜI DÙNG",
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
                                    case 0 -> {
                                        richText.applyFont(0, 14, boldFont);
                                        richText.applyFont(14, 16, redBoldFont);
                                        richText.applyFont(17, header.length(), italicFont);
                                    }
                                    case 1 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(5, 7, redBoldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 2 -> {
                                        richText.applyFont(0, 8, boldFont);
                                        richText.applyFont(8, 10, redBoldFont);
                                        richText.applyFont(11, header.length(), italicFont);
                                    }
                                    case 11 -> {
                                        richText.applyFont(0, 10, boldFont);
                                        richText.applyFont(10, 12, redBoldFont);
                                        richText.applyFont(13, header.length(), italicFont);
                                    }
                                    case 13 -> {
                                        richText.applyFont(0, 7, boldFont);
                                        richText.applyFont(7, 9, redBoldFont);
                                        richText.applyFont(10, header.length(), italicFont);
                                    }
                                    case 3, 9 -> {
                                        richText.applyFont(0, 10, boldFont);
                                        richText.applyFont(11, header.length(), italicFont);
                                    }
                                    case 4 -> {
                                        richText.applyFont(0, 2, boldFont);
                                        richText.applyFont(3, header.length(), italicFont);
                                    }
                                    case 5 -> {
                                        richText.applyFont(0, 13, boldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                    case 6 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 7 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(5, header.length(), italicFont);
                                    }
                                    case 8 -> {
                                        richText.applyFont(0, 22, boldFont);
                                        richText.applyFont(23, header.length(), italicFont);
                                    }
                                    case 10, 12 -> {
                                        richText.applyFont(boldFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                buildRoleSheets()
        );
        UtilsExcel.exportToExcel(
                response,
                sheets
        );
    }

    @Override
    @PreAuthorize("hasAuthority('USER_SEARCH')")
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
                "Tên người dùng * \n(Tối đa 100 kí tự)",
                "Email * \n(Tối đa 100 kí tự, nhập đúng định dạng email)",
                "Tên đầy đủ \n(Tối đa 100 kí tự)",
                "Mã \n(Tối đa 100 kí tự)",
                "Số điện thoại \n(Tối đa 100 kí tự, chỉ nhập số)",
                "Ngành \n(Tối đa 100 kí tự)",
                "Khóa \n(Tối đa 100 kí tự)",
                "Vai trò của người dùng \n(Tối đa 100 kí tự)",
                "Giới tính \n(Tối đa 100 kí tự)",
                "Ngày sinh",
                "Trạng thái * \n(Chọn ít nhất 1 trạng thái)",
                "Xác thực hai lớp",
                "Vai trò * \n(Phải chọn ít nhất 1 Vai trò)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                7, new String[]{"STUDENT", "LECTURER", "LIBRARIAN", "ADMIN"},
                8, new String[]{"MALE", "FEMALE"},
                10, new String[]{"ACTIVE", "SUSPENDED", "BANNED", "DELETED"}
        );
        Map<Integer, Integer> widths = Map.ofEntries(
                Map.entry(0, 7000),
                Map.entry(1, 7000),
                Map.entry(2, 7000),
                Map.entry(3, 7000),
                Map.entry(4, 9000),
                Map.entry(5, 7000),
                Map.entry(6, 7000),
                Map.entry(7, 7000),
                Map.entry(8, 7000),
                Map.entry(9, 7000),
                Map.entry(10, 7000),
                Map.entry(11, 7000),
                Map.entry(12, 7000)
        );
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH NGƯỜI DÙNG",
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
                                        richText.applyFont(0, 14, boldFont);
                                        richText.applyFont(14, 16, redBoldFont);
                                        richText.applyFont(17, header.length(), italicFont);
                                    }
                                    case 1 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(5, 7, redBoldFont);
                                        richText.applyFont(7, header.length(), italicFont);
                                    }
                                    case 10 -> {
                                        richText.applyFont(0, 10, boldFont);
                                        richText.applyFont(10, 12, redBoldFont);
                                        richText.applyFont(13, header.length(), italicFont);
                                    }
                                    case 12 -> {
                                        richText.applyFont(0, 7, boldFont);
                                        richText.applyFont(7, 9, redBoldFont);
                                        richText.applyFont(10, header.length(), italicFont);
                                    }
                                    case 2, 8 -> {
                                        richText.applyFont(0, 10, boldFont);
                                        richText.applyFont(11, header.length(), italicFont);
                                    }
                                    case 3 -> {
                                        richText.applyFont(0, 2, boldFont);
                                        richText.applyFont(3, header.length(), italicFont);
                                    }
                                    case 4 -> {
                                        richText.applyFont(0, 13, boldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                    case 5 -> {
                                        richText.applyFont(0, 5, boldFont);
                                        richText.applyFont(6, header.length(), italicFont);
                                    }
                                    case 6 -> {
                                        richText.applyFont(0, 4, boldFont);
                                        richText.applyFont(5, header.length(), italicFont);
                                    }
                                    case 7 -> {
                                        richText.applyFont(0, 22, boldFont);
                                        richText.applyFont(23, header.length(), italicFont);
                                    }
                                    case 9, 11 -> {
                                        richText.applyFont(boldFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                buildRoleSheets()
        );
        UtilsExcel.exportToExcel(
                response,
                sheets
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('USER_CREATE')")
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
                List<String> values = new ArrayList<>();
                for (int c = 0; c <= 13; c++) {
                    values.add(UtilsExcel.getCellValue(row.getCell(c)).trim());
                }
                UserRequest request = new UserRequest();
                request.setUsername(values.get(0));
                request.setEmail(values.get(1));
                request.setPassword(values.get(2).trim());
                request.setFullName(values.get(3));
                request.setCode(values.get(4));
                request.setPhone(Utils.getCellValue(row.getCell(5)));
                request.setMajor(values.get(6));
                request.setCourse(values.get(7));
                request.setPosition(values.get(8) != null && !values.get(8).isEmpty() ? Position.valueOf(values.get(8)) : null);
                request.setGender(values.get(9) != null && !values.get(9).isEmpty() ? Gender.valueOf(values.get(9)) : null);
                Cell cell = row.getCell(10);
                LocalDate newDate = null;
                if (cell != null) {
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        newDate = cell.getLocalDateTimeCellValue().toLocalDate();
                    } else {
                        String raw = UtilsExcel.getCellValue(cell);
                        if (raw != null && !raw.isEmpty()) {
                            newDate = LocalDate.parse(raw);
                        }
                    }
                }
                request.setDob(newDate);
                request.setStatus(values.get(11) != null && !values.get(11).isEmpty() ? AccountStatus.valueOf(values.get(11)) : null);
                request.setTwoFactorEnabled(values.get(12) != null && !values.get(12).trim().isEmpty() && Boolean.parseBoolean(values.get(12)));
                String roleStr = values.get(13);
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
                Utils.checkDuplicate(request.getUsername(), existingUsernames, excelUsernames);
                Utils.checkDuplicate(request.getEmail(), existingEmails, excelEmails);
                Utils.checkDuplicate(request.getCode(), existingCodes, excelCodes);
                Utils.checkDuplicate(request.getPhone(), existingPhones, excelPhones);
                User user = getUser(request, roles);
                users.add(user);
            }
            userRepository.saveAll(users);
            workbook.close();
        } catch (IOException e) {
            throw new AppException(UserErrorCode.USER_NOT_READ_FILE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('USER_SEARCH')")
    public List autoSearch(UserAutoSearch keyword) {
        if (keyword.getKeyword() == null || keyword.getKeyword().trim().isEmpty() || keyword.getType() == null || keyword.getType().trim().isEmpty()) {
            return Collections.emptyList();
        }
        String column = getString(keyword);
        String sql = "SELECT DISTINCT " + column +
                " FROM users WHERE LOWER(" + column + ") LIKE LOWER(CONCAT('%', :keyword, '%'))";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("keyword", keyword.getKeyword().trim());
        return query.getResultList();
    }
}

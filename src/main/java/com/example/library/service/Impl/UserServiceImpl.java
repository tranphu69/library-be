package com.example.library.service.Impl;

import com.example.library.dto.request.user.UserCreateRequest;
import com.example.library.dto.request.user.UserListRequest;
import com.example.library.dto.request.user.UserUpdateRequest;
import com.example.library.dto.response.Permission.PermissionResponse;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        Sort sort = Utils.createSortUsername(request.getSortBy(), request.getSortType());
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

    @Override
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Thông tin danh sách");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH USER");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(true);
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(60);
        String[] headers = {
                "Email * \n(Tối đa 50 kí tự)",
                "Username * \n(Tối đa 50 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)",
                "Roles"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            XSSFRichTextString richText = new XSSFRichTextString(headers[i]);
            Font fontTitle = workbook.createFont();
            Font fontStar = workbook.createFont();
            Font fontDesc = workbook.createFont();
            switch (i) {
                case 0:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 5, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(5, 7, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(8, headers[i].length(), fontDesc);
                    break;
                case 1:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 8, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(8, 10, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(11, headers[i].length(), fontDesc);
                    break;
                case 2:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 10, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(10, 12, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(13, headers[i].length(), fontDesc);
                    break;
                case 3:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 5, fontTitle);
                    break;
            }
            cell.setCellValue(richText);
            cell.setCellStyle(headerStyle);
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(2, 10000);
        sheet.setColumnWidth(3, 10000);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                new String[]{"Hoạt động", "Không hoạt động"}
        );
        CellRangeAddressList addressList = new CellRangeAddressList(4, 15, 2, 2);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        if (validation instanceof org.apache.poi.xssf.usermodel.XSSFDataValidation) {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }
        sheet.addValidationData(validation);
        List<RoleResponse> roleResponses = roleRepository.findAllStatus1()
                .stream().map(r -> {
                    RoleResponse roleResponse = new RoleResponse();
                    roleResponse.setName(r.getName());
                    roleResponse.setDescription(r.getDescription());
                    roleResponse.setStatus(r.getStatus());
                    return roleResponse;
                }).toList();
        Sheet sheet1 = workbook.createSheet("Danh sách role");
        Row titleRow1 = sheet1.createRow(1);
        titleRow1.setHeightInPoints(30);
        Cell titleCell1 = titleRow1.createCell(0);
        titleCell1.setCellValue("DANH SÁCH ROLE");
        titleCell1.setCellStyle(titleStyle);
        sheet1.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
        Row headerRow1 = sheet1.createRow(3);
        String[] headers1 = { "Tên", "Mô tả", "Trạng thái" };
        headerRow1.setHeightInPoints(60);
        for (int i = 0; i < headers1.length; i++) {
            Cell cell = headerRow1.createCell(i);
            XSSFRichTextString richText = new XSSFRichTextString(headers1[i]);
            Font fontTitle = workbook.createFont();
            fontTitle.setBold(true);
            fontTitle.setColor(IndexedColors.BLACK.getIndex());
            cell.setCellValue(richText);
            cell.setCellStyle(headerStyle);
        }
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        int rowIndex = 4;
        for (RoleResponse p : roleResponses) {
            Row row = sheet1.createRow(rowIndex++);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(p.getName());
            cell0.setCellStyle(dataStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(p.getDescription());
            cell1.setCellStyle(dataStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(p.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
            cell2.setCellStyle(dataStyle);
        }
        for (int i = 0; i < headers1.length; i++) {
            sheet1.autoSizeColumn(i);
        }
        sheet1.setColumnWidth(0, 8000);
        sheet1.setColumnWidth(1, 12000);
        sheet1.setColumnWidth(2, 10000);
        workbook.write(response.getOutputStream());
        workbook.close();
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

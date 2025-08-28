package com.example.library.service.Impl;

import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleListRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.dto.response.role.RoleListResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Role create(RoleCreateRequest request) {
        if(roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXSITED);
        }
        List<Permission> permissions = permissionRepository.findAllActiveById(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    public Role update(RoleUpdateRequest request) {
        Role role = roleRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXSITED));
        List<Permission> permissions = permissionRepository.findAllActiveById(request.getPermissions());
        if (permissions.size() != request.getPermissions().size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    public void delete(List<Long> ids) {
        List<Role> roles = roleRepository.findAllById(ids);
        if(roles.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        List<Long> deleteIds = roles.stream()
                .filter(p -> p.getStatus() == -1)
                .map(Role::getId)
                .toList();
        if(!deleteIds.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        for(Role role : roles){
            role.setStatus(-1);
        }
        roleRepository.saveAll(roles);
    }

    @Override
    public Role detail(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXSITED));
        if (role.getStatus() == -1) {
            throw new AppException(ErrorCode.ROLE_NOT_EXSITED);
        }
        return role;
    }

    @Override
    public List<RoleResponse> getListAutoSearch(String keyword) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> rolePage = roleRepository.search(keyword, pageable);
        return rolePage.getContent()
                .stream()
                .map(role -> {
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    response.setStatus(role.getStatus());
                    response.setPermissions(role.getPermissions());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoleListResponse getList(RoleListRequest request) {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = convertToLongList(request.getPermissions());
        List<Permission> permissions = permissionRepository.findAllActiveById(arrNumber);
        if (permissions.size() != arrNumber.size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        Page<Role> rolePage = roleRepository.findRolesWithFilter(
                request.getName(),
                request.getStatus(),
                arrNumber,
                pageable
        );
        List<RoleResponse> roleResponses = getListRole(rolePage);
        RoleListResponse response = new RoleListResponse();
        response.setData(roleResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(rolePage.getSize());
        response.setTotalPages(rolePage.getTotalPages());
        response.setTotalElements((int) rolePage.getTotalElements());
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
        titleCell.setCellValue("DANH SÁCH ROLE");
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)", "Permissions * \n(Chọn ít nhất 1 permission)"};
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
                    richText.applyFont(0, 3, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(3, 5, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(6, headers[i].length(), fontDesc);
                    break;
                case 1:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 5, fontTitle);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(6, headers[i].length(), fontDesc);
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
                    richText.applyFont(0, 11, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(11, 13, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(14, headers[i].length(), fontDesc);
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
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public void exportToExcel(RoleListRequest request, HttpServletResponse response) throws IOException {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = convertToLongList(request.getPermissions());
        List<Permission> permissions = permissionRepository.findAllActiveById(arrNumber);
        if (permissions.size() != arrNumber.size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        Page<Role> rolePage = roleRepository.findRolesWithFilter(
                request.getName(),
                request.getStatus(),
                arrNumber,
                pageable
        );
        List<RoleResponse> roleResponses = getListRole(rolePage);
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
        titleCell.setCellValue("DANH SÁCH ROLE");
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)", "Permissions * \n(Chọn ít nhất 1 permission)"};
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
                    richText.applyFont(0, 3, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(3, 5, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(6, headers[i].length(), fontDesc);
                    break;
                case 1:
                    fontTitle.setBold(true);
                    fontTitle.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(0, 5, fontTitle);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(6, headers[i].length(), fontDesc);
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
                    richText.applyFont(0, 11, fontTitle);
                    fontStar.setBold(true);
                    fontStar.setColor(IndexedColors.RED.getIndex());
                    richText.applyFont(11, 13, fontStar);
                    fontDesc.setItalic(true);
                    fontDesc.setColor(IndexedColors.BLACK.getIndex());
                    richText.applyFont(14, headers[i].length(), fontDesc);
                    break;
            }
            cell.setCellValue(richText);
            cell.setCellStyle(headerStyle);
        }
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        int rowIndex = 4;
        for (RoleResponse r : roleResponses) {
            Row row = sheet.createRow(rowIndex++);
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(r.getName());
            cell0.setCellStyle(dataStyle);
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(r.getDescription());
            cell1.setCellStyle(dataStyle);
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(r.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
            cell2.setCellStyle(dataStyle);
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(r.getPermissions()
                    .stream()
                    .map(Permission::getName)
                    .collect(Collectors.joining(", ")));
            cell3.setCellStyle(dataStyle);
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
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private List<Long> convertToLongList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private List<RoleResponse> getListRole(Page<Role> rolePage) {
        return rolePage.getContent()
                .stream()
                .map(role -> {
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    response.setStatus(role.getStatus());
                    Set<Permission> activePermissions = role.getPermissions()
                            .stream()
                            .filter(p -> p.getStatus() != -1)
                            .collect(Collectors.toSet());
                    response.setPermissions(activePermissions);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private boolean isValidSortField(String sortBy) {
        List<String> allowedFields = List.of("name", "status");
        return allowedFields.contains(sortBy);
    }

    private Sort createSort(String sortBy, String sortType) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "name";
        }
        if (!isValidSortField(sortBy)) {
            sortBy = "name";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortType)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}

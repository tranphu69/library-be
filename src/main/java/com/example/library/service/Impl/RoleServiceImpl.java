package com.example.library.service.Impl;

import com.example.library.dto.request.role.RoleCreateRequest;
import com.example.library.dto.request.role.RoleListRequest;
import com.example.library.dto.request.role.RoleUpdateRequest;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.dto.response.role.RoleListResponse;
import com.example.library.dto.response.role.RoleResponse;
import com.example.library.dto.response.role.RoleResponseNoPermission;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.enums.PermissionErrorCode;
import com.example.library.enums.RoleErrorCode;
import com.example.library.exception.AppException;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
import com.example.library.utils.Utils;
import com.example.library.utils.UtilsExcel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
        if(request.getStatus() == 1 && request.getPermissions().isEmpty()) {
            throw new AppException(RoleErrorCode.ROLE_STATUS_1);
        }
        if(roleRepository.existsByName(request.getName())) {
            throw new AppException(RoleErrorCode.ROLE_EXSITED);
        }
        List<Permission> permissions = new ArrayList<>();
        if (!request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findAllActiveById(request.getPermissions());
            if (permissions.size() != request.getPermissions().size()) {
                throw new AppException(PermissionErrorCode.PERMISSION_NOT_EXSITED);
            }
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
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NOT_EXSITED));
        if(roleRepository.existsByNameAndIdNot(request.getName(), request.getId())) {
            throw new AppException(RoleErrorCode.ROLE_EXSITED);
        }
        if(request.getStatus() == 1 && request.getPermissions().isEmpty()) {
            throw new AppException(RoleErrorCode.ROLE_STATUS_1);
        }
        List<Permission> permissions = new ArrayList<>();
        if (!request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findAllActiveById(request.getPermissions());
            if (permissions.size() != request.getPermissions().size()) {
                throw new AppException(PermissionErrorCode.PERMISSION_NOT_EXSITED);
            }
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
            throw new AppException(RoleErrorCode.ROLE_NOT_EXSITED);
        }
        List<Long> deleteIds = roles.stream()
                .filter(p -> p.getStatus() == -1)
                .map(Role::getId)
                .toList();
        if(!deleteIds.isEmpty()){
            throw new AppException(RoleErrorCode.ROLE_NOT_EXSITED);
        }
        for(Role role : roles){
            role.setStatus(-1);
        }
        roleRepository.saveAll(roles);
    }

    @Override
    public Role detail(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NOT_EXSITED));
        if (role.getStatus() == -1) {
            throw new AppException(RoleErrorCode.ROLE_NOT_EXSITED);
        }
        Set<Permission> activePermissions = role.getPermissions()
                .stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toSet());
        role.setPermissions(activePermissions);
        return role;
    }

    @Override
    public List<RoleResponseNoPermission> getListAutoSearch(String keyword) {
        String normalizedQuery = keyword.trim();
        List<Role> roles = roleRepository.findByNameStartingWithIgnoreCase(normalizedQuery);
        return roles.stream()
                .map(role -> {
                    RoleResponseNoPermission response = new RoleResponseNoPermission();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoleListResponse getList(RoleListRequest request) {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = Utils.convertToLongList(request.getPermissions());
        List<Permission> permissions = permissionRepository.findAllActiveById(arrNumber);
        if (permissions.size() != arrNumber.size()) {
            throw new AppException(PermissionErrorCode.PERMISSION_NOT_EXSITED);
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)", "Permissions * \n(Chọn ít nhất 1 permission trong danh sách permission nếu trạng thái là hoạt động)"};
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
        List<PermissionResponse> permissionResponses = permissionRepository.findAllStatus1()
                .stream().map(p -> {
                    PermissionResponse permissionResponse = new PermissionResponse();
                    permissionResponse.setName(p.getName());
                    permissionResponse.setDescription(p.getDescription());
                    permissionResponse.setStatus(p.getStatus());
                    return permissionResponse;
                }).toList();
        Sheet sheet1 = workbook.createSheet("Danh sách permission");
        Row titleRow1 = sheet1.createRow(1);
        titleRow1.setHeightInPoints(30);
        Cell titleCell1 = titleRow1.createCell(0);
        titleCell1.setCellValue("DANH SÁCH PERMISSION");
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
        for (PermissionResponse p : permissionResponses) {
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
        for (int i = 0; i < headers.length - 1; i++) {
            sheet1.autoSizeColumn(i);
        }
        sheet1.setColumnWidth(0, 8000);
        sheet1.setColumnWidth(1, 12000);
        sheet1.setColumnWidth(2, 10000);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public void exportToExcel(RoleListRequest request, HttpServletResponse response) throws IOException {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        List<Long> arrNumber = Utils.convertToLongList(request.getPermissions());
        List<Permission> permissions = permissionRepository.findAllActiveById(arrNumber);
        if (permissions.size() != arrNumber.size()) {
            throw new AppException(PermissionErrorCode.PERMISSION_NOT_EXSITED);
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)", "Permissions * \n(Chọn ít nhất 1 permission trong danh sách permission nếu trạng thái là hoạt động)"};
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
        List<PermissionResponse> permissionResponses = permissionRepository.findAllStatus1()
                .stream().map(p -> {
                    PermissionResponse permissionResponse = new PermissionResponse();
                    permissionResponse.setName(p.getName());
                    permissionResponse.setDescription(p.getDescription());
                    permissionResponse.setStatus(p.getStatus());
                    return permissionResponse;
                }).toList();
        Sheet sheet1 = workbook.createSheet("Danh sách permission");
        Row titleRow1 = sheet1.createRow(1);
        titleRow1.setHeightInPoints(30);
        Cell titleCell1 = titleRow1.createCell(0);
        titleCell1.setCellValue("DANH SÁCH PERMISSION");
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
        int rowIndex1 = 4;
        for (PermissionResponse p : permissionResponses) {
            Row row = sheet1.createRow(rowIndex1++);
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

    @Override
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()){
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Set<String> excelNames = new HashSet<>();
            List<Role> roles = new ArrayList<>();
            for(int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(UtilsExcel.isRowEmpty(row)) continue;
                RoleCreateRequest request = new RoleCreateRequest();
                request.setName(UtilsExcel.getCellValue(row.getCell(0)));
                request.setDescription(UtilsExcel.getCellValue(row.getCell(1)));
                String statusStr = UtilsExcel.getCellValue(row.getCell(2));
                Integer status = statusStr.isEmpty() ? null : statusStr.trim().equals("Hoạt động") ? 1 : 0;
                request.setStatus(status);
                String permissionStr = UtilsExcel.getCellValue(row.getCell(3));
                List<String> permissionArr = Arrays.stream(permissionStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                Set<Permission> permissions = permissionRepository.findByNameIn(permissionArr);
                Set<ConstraintViolation<RoleCreateRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                if (!excelNames.add(request.getName())){
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                if (roleRepository.existsByName(request.getName())){
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                Role role = new Role();
                role.setName(request.getName());
                role.setDescription(request.getDescription());
                role.setStatus(request.getStatus());
                role.setPermissions(new HashSet<>(permissions));
                roles.add(role);
            }
            roleRepository.saveAll(roles);
            workbook.close();
        } catch (IOException e) {
            throw new AppException(RoleErrorCode.ROLE_NOT_READ_FILE);
        }
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
                            .filter(p -> p.getStatus() != -1 && p.getStatus() != 0)
                            .collect(Collectors.toSet());
                    response.setPermissions(activePermissions);
                    return response;
                })
                .collect(Collectors.toList());
    }
}
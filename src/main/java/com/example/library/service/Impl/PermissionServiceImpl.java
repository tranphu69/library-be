package com.example.library.service.Impl;

import com.example.library.dto.request.permission.PermissionCreateRequest;
import com.example.library.dto.request.permission.PermissionListRequest;
import com.example.library.dto.request.permission.PermissionUpdateRequest;
import com.example.library.dto.response.Permission.PermissionListResponse;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.exception.AppException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.service.PermissionService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private Validator validator;

    @Override
    public Permission create(PermissionCreateRequest request) {
        if(permissionRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.PERMISSION_EXSITED);
        }
        Permission permission = new Permission();
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        return permissionRepository.save(permission);
    }

    @Override
    public Permission update(PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXSITED));
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        return permissionRepository.save(permission);
    }

    @Override
    public PermissionListResponse getList(PermissionListRequest request) {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getStatus(),
                pageable
        );
        List<PermissionResponse> permissionResponses = getListPermission(request, permissionPage);
        PermissionListResponse response = new PermissionListResponse();
        response.setData(permissionResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(permissionPage.getSize());
        response.setTotalPages(permissionPage.getTotalPages());
        response.setTotalElements((int) permissionPage.getTotalElements());
        return response;
    }

    @Override
    public List<PermissionResponse> getListAutoSearch(String keyword) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> permissionPage = permissionRepository.search(keyword, pageable);
        return permissionPage.getContent()
                .stream()
                .map(permission -> {
                    PermissionResponse response = new PermissionResponse();
                    response.setId(permission.getId());
                    response.setName(permission.getName());
                    response.setDescription(permission.getDescription());
                    response.setStatus(permission.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void exportToExcel(PermissionListRequest request, HttpServletResponse response) throws IOException {
        Sort sort = createSort(request.getSortBy(), request.getSortType());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getStatus(),
                pageable
        );
        List<PermissionResponse> permissionResponses = getListPermission(request, permissionPage);
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
        titleCell.setCellValue("DANH SÁCH PERMISSION");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"};
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
            }
            cell.setCellValue(richText);
            cell.setCellStyle(headerStyle);
        }
        int rowIndex = 4;
        for (PermissionResponse p : permissionResponses) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(p.getName());
            row.createCell(1).setCellValue(p.getDescription());
            row.createCell(2).setCellValue(p.getStatus() == 1 ? "Hoạt động" : "Không hoạt động");
        }
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(2, 10000);
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                new String[]{"Hoạt động", "Không hoạt động"}
        );
        CellRangeAddressList addressList = new CellRangeAddressList(4, permissionResponses.toArray().length + 3, 2, 2);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        if (validation instanceof org.apache.poi.xssf.usermodel.XSSFDataValidation) {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }
        sheet.addValidationData(validation);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private List<PermissionResponse> getListPermission(PermissionListRequest request, Page<Permission> permissionPage) {
        return permissionPage.getContent()
                .stream()
                .map(permission -> {
                    PermissionResponse response = new PermissionResponse();
                    response.setId(permission.getId());
                    response.setName(permission.getName());
                    response.setDescription(permission.getDescription());
                    response.setStatus(permission.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
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

    private boolean isValidSortField(String sortBy) {
        List<String> allowedFields = List.of("name", "status");
        return allowedFields.contains(sortBy);
    }

    @Override
    @Transactional
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Set<String> excelNames = new HashSet<>();
            List<Permission> permissions = new ArrayList<>();
            for(int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(isRowEmpty(row)) continue;
                PermissionCreateRequest request = new PermissionCreateRequest();
                request.setName(getCellValue(row.getCell(0)));
                request.setDescription(getCellValue(row.getCell(1)));
                String statusStr = getCellValue(row.getCell(2));
                Integer status = statusStr.isEmpty() ? null : statusStr.trim().equals("Hoạt động") ? 1 : 0;
                request.setStatus(status);
                Set<ConstraintViolation<PermissionCreateRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new AppException(ErrorCode.PERMISSION_ERROR_FILE);
                }
                if (!excelNames.add(request.getName())){
                    throw new AppException(ErrorCode.PERMISSION_ERROR_FILE);
                }
                if (permissionRepository.existsByName(request.getName())){
                    throw new AppException(ErrorCode.PERMISSION_ERROR_FILE);
                }
                Permission permission = new Permission();
                permission.setName(request.getName());
                permission.setDescription(request.getDescription());
                permission.setStatus(request.getStatus());
                permissions.add(permission);
            }
            permissionRepository.saveAll(permissions);
            workbook.close();
        } catch (IOException e) {
            throw new AppException(ErrorCode.PERMISSION_NOT_READ_FILE);
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    @Override
    public void delete(List<Long> ids) {
        List<Permission> permissions = permissionRepository.findAllById(ids);
        if (permissions.isEmpty()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        List<Long> deletedIds = permissions.stream()
                .filter(p -> p.getStatus() == -1)
                .map(Permission::getId)
                .toList();
        if (!deletedIds.isEmpty()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        for (Permission permission : permissions) {
            permission.setStatus(-1);
        }
        permissionRepository.saveAll(permissions);
    }

    @Override
    public Permission detail(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXSITED));
        if (permission.getStatus() == -1) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXSITED);
        }
        return permission;
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
        titleCell.setCellValue("DANH SÁCH PERMISSION");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));
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
        String[] headers = {"Tên * \n(Tối đa 50 kí tự)", "Mô tả \n(Tối đa 255 kí tự)", "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"};
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
}
package com.example.library.service.Impl;

import com.example.library.dto.request.Permission.PermissionListRequest;
import com.example.library.dto.request.Permission.PermissionRequest;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.exception.AppException;
import com.example.library.exception.messageError.PermissionErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.PermissionService;
import com.example.library.utils.Utils;
import com.example.library.utils.UtilsExcel;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionSericeImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private RoleRepository roleRepository;

    private List<PermissionResponse> getListPermission(Page<Permission> permissionPage) {
        return permissionPage.getContent()
                .stream()
                .map(permission -> {
                    PermissionResponse response = new PermissionResponse();
                    response.setId(permission.getId());
                    response.setName(permission.getName());
                    response.setDescription(permission.getDescription());
                    response.setAction(permission.getAction());
                    response.setCreatedAt(permission.getCreatedAt());
                    response.setUpdatedAt(permission.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Permission create(PermissionRequest request) {
        String newName = request.getName().trim();
        if (permissionRepository.existsByName(newName)) {
            throw new AppException(PermissionErrorCode.PERMISSION_EXSITED);
        }
        Permission permission = new Permission();
        permission.setName(newName);
        permission.setDescription(request.getDescription());
        permission.setAction(request.getAction());
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public Permission update(PermissionRequest request, Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED));
        String newName = request.getName().trim();
        if (permissionRepository.existsByNameAndIdNot(newName, id)) {
            throw new AppException(PermissionErrorCode.PERMISSION_EXSITED);
        }
        if (Objects.equals(permission.getName(), newName) && Objects.equals(permission.getDescription(), request.getDescription())
                && Objects.equals(permission.getAction(), request.getAction())) {
            return permission;
        }
        boolean isUsed = roleRepository.existsByPermissions_Id(id);
        if (isUsed && request.getAction() == 0) {
            throw new AppException(PermissionErrorCode.PERMISSION_IN_USE_BY_ROLE);
        }
        permission.setName(newName);
        permission.setDescription(request.getDescription());
        permission.setAction(request.getAction());
        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        List<Permission> permissions = permissionRepository.findAllById(ids);
        List<Long> deletedIds = permissions.stream()
                .filter(p -> p.getAction() != -1)
                .map(Permission::getId)
                .toList();
        if (deletedIds.isEmpty()) {
            throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
        }
        List<Role> rolesUsingPermissions  = roleRepository.findAllByPermissionIds(ids);
        if (!rolesUsingPermissions.isEmpty()) {
            throw new AppException(PermissionErrorCode.PERMISSION_IN_USE_BY_ROLE);
        }
        for (Permission permission : permissions) {
            permission.setAction(-1);
        }
        permissionRepository.saveAll(permissions);
    }

    @Override
    public Permission detail(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED));
        if (permission.getAction() == -1) {
            throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
        }
        return permission;
    }

    @Override
    public PageResponse<PermissionResponse> getList(PermissionListRequest request) {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("name", "action", "createdAt", "updatedAt", "createdBy", "updatedBy"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getAction(),
                pageable
        );
        List<PermissionResponse> permissionResponses = getListPermission(permissionPage);
        PageResponse<PermissionResponse> response = new PageResponse<>();
        response.setData(permissionResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(permissionPage.getSize());
        response.setTotalPages(permissionPage.getTotalPages());
        response.setTotalElements((int) permissionPage.getTotalElements());
        return response;
    }

    @Override
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        List<PermissionResponse> permissionResponses = new ArrayList<>();
        List<String> headers = List.of(
                "Tên * \n(Tối đa 100 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths = Map.of(
                0, 8000,
                1, 12000,
                2, 10000
        );
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH QUYỀN THAO TÁC",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        permissionResponses,
                        p -> List.of(
                                p.getName(),
                                p.getDescription(),
                                p.getAction() == 1 ? "Hoạt động" : "Không hoạt động"
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
    public void exportToExcel(PermissionListRequest request, HttpServletResponse response) throws IOException {
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("name", "action", "createdAt", "updatedAt", "createdBy", "updatedBy"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Permission> permissionPage = permissionRepository.findPermissionsWithFilters(
                request.getName(),
                request.getAction(),
                pageable
        );
        List<PermissionResponse> permissionResponses = getListPermission(permissionPage);
        List<String> headers = List.of(
                "Tên * \n(Tối đa 50 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths = Map.of(
                0, 8000,
                1, 12000,
                2, 10000
        );
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH QUYỀN THAO TÁC",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        permissionResponses,
                        p -> List.of(
                                p.getName(),
                                p.getDescription(),
                                p.getAction() == 1 ? "Hoạt động" : "Không hoạt động"
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
    @Transactional
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Set<String> existingNames = new HashSet<>(permissionRepository.findAllNames());
            Set<String> excelNames = new HashSet<>();
            List<Permission> permissions = new ArrayList<>();
            for(int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(UtilsExcel.isRowEmpty(row)) continue;
                PermissionRequest request = new PermissionRequest();
                request.setName(UtilsExcel.getCellValue(row.getCell(0)));
                request.setDescription(UtilsExcel.getCellValue(row.getCell(1)));
                String statusStr = UtilsExcel.getCellValue(row.getCell(2));
                Integer status = statusStr.isEmpty() ? null : statusStr.trim().equals("Hoạt động") ? 1 : 0;
                request.setAction(status);
                Set<ConstraintViolation<PermissionRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new AppException(PermissionErrorCode.PERMISSION_ERROR_FILE);
                }
                if (!excelNames.add(request.getName())){
                    throw new AppException(PermissionErrorCode.PERMISSION_ERROR_FILE);
                }
                if (existingNames.contains(request.getName())){
                    throw new AppException(PermissionErrorCode.PERMISSION_ERROR_FILE);
                }
                Permission permission = new Permission();
                permission.setName(request.getName());
                permission.setDescription(request.getDescription());
                permission.setAction(request.getAction());
                permissions.add(permission);
            }
            permissionRepository.saveAll(permissions);
            workbook.close();
        } catch (IOException e) {
            throw new AppException(PermissionErrorCode.PERMISSION_NOT_READ_FILE);
        }
    }

    @Override
    public List<String> autoSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = permissionRepository.findNamesByKeyword(keyword);
        return results != null ? results : Collections.emptyList();
    }
}

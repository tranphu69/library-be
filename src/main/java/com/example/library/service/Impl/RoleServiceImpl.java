package com.example.library.service.Impl;

import com.example.library.dto.request.Role.RoleListRequest;
import com.example.library.dto.request.Role.RoleRequest;
import com.example.library.dto.response.Permission.PermissionNoAction;
import com.example.library.dto.response.Permission.PermissionResponse;
import com.example.library.dto.response.Role.RoleListResponse;
import com.example.library.dto.response.Role.RoleResponse;
import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.exception.AppException;
import com.example.library.exception.enums.PermissionErrorCode;
import com.example.library.exception.enums.RoleErrorCode;
import com.example.library.repository.PermissionRepository;
import com.example.library.repository.RoleRepository;
import com.example.library.service.RoleService;
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
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private Validator validator;
    @Autowired
    private PermissionRepository permissionRepository;

    private List<RoleResponse> getListRole(Page<Role> rolePage) {
        return rolePage.getContent()
                .stream()
                .map(role -> {
                    Set<PermissionNoAction> noActionSet = role.getPermissions().stream()
                            .map(p -> {
                                PermissionNoAction dto = new PermissionNoAction();
                                dto.setId(p.getId());
                                dto.setName(p.getName());
                                dto.setDescription(p.getDescription());
                                return dto;
                            })
                            .collect(Collectors.toSet());
                    RoleResponse response = new RoleResponse();
                    response.setId(role.getId());
                    response.setName(role.getName());
                    response.setDescription(role.getDescription());
                    response.setAction(role.getAction());
                    response.setPermissions(noActionSet);
                    response.setCreatedAt(role.getCreatedAt());
                    response.setUpdatedAt(role.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Role create(RoleRequest request) {
        String newName = request.getName().trim();
        if (roleRepository.existsByName(newName)) {
            throw new AppException(RoleErrorCode.ROLE_EXSITED);
        }
        List<Permission> permissions = new ArrayList<>();
        if (!request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findAllActiveById(request.getPermissions());
            if (permissions.size() != request.getPermissions().size()) {
                throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
            }
        }
        Role role = new Role();
        role.setName(newName);
        role.setDescription(request.getDescription());
        role.setAction(request.getAction());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role update(RoleRequest request, Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NO_EXSITED));
        String newName = request.getName().trim();
        if (roleRepository.existsByNameAndIdNot(newName, id)) {
            throw new AppException(RoleErrorCode.ROLE_EXSITED);
        }
        List<Permission> permissions = new ArrayList<>();
        if (!request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findAllActiveById(request.getPermissions());
            if (permissions.size() != request.getPermissions().size()) {
                throw new AppException(PermissionErrorCode.PERMISSION_NO_EXSITED);
            }
        }
        Set<Long> existingPermissionIds = role.getPermissions()
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        Set<Long> newPermissionIds = new HashSet<>(request.getPermissions());
        if (Objects.equals(role.getName(), newName)
                && Objects.equals(role.getDescription(), request.getDescription())
                && Objects.equals(role.getAction(), request.getAction())
                && existingPermissionIds.equals(newPermissionIds)
        ) {
            return role;
        }
        role.setName(newName);
        role.setDescription(request.getDescription());
        role.setAction(request.getAction());
        role.setPermissions(new HashSet<>(permissions));
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        List<Role> roles = roleRepository.findAllById(ids);
        List<Long> deletedIds = roles.stream()
                .filter(p -> p.getAction() != -1)
                .map(Role::getId)
                .toList();
        if (deletedIds.isEmpty()) {
            throw new AppException(RoleErrorCode.ROLE_NO_EXSITED);
        }
        for (Role role : roles) {
            role.setAction(-1);
        }
        roleRepository.saveAll(roles);
    }

    @Override
    public Role detail(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(RoleErrorCode.ROLE_NO_EXSITED));
        if (role.getAction() == -1) {
            throw new AppException(RoleErrorCode.ROLE_NO_EXSITED);
        }
        return role;
    }

    @Override
    public RoleListResponse getList(RoleListRequest request) {
        List<Long> permissionIds = Arrays.stream(request.getPermissions().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("name", "action", "createdAt", "updatedAt", "createdBy", "updatedBy"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<Role> rolePage = roleRepository.findRolesWithAllPermissions(
                request.getName(),
                request.getAction(),
                permissionIds,
                pageable
        );
        List<RoleResponse> roleResponses = getListRole(rolePage);
        RoleListResponse response = new RoleListResponse();
        response.setData(roleResponses);
        response.setCurrentPage(request.getPage());
        response.setCurrentSize(request.getSize());
        response.setTotalPages(rolePage.getTotalPages());
        response.setTotalElements((int) rolePage.getTotalElements());
        return response;
    }

    @Override
    public void exportTemplateExcel(HttpServletResponse response) throws IOException {
        List<RoleResponse> roleResponses = new ArrayList<>();
        List<String> headers = List.of(
                "Tên * \n(Tối đa 100 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)",
                "Permission * \n(Phải chọn ít nhất 1 permission)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths = Map.of(
                0, 8000,
                1, 12000,
                2, 10000,
                3, 12000
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
        List<PermissionResponse> permissionResponses = permissionRepository.findAllStatus1()
                .stream().map(p -> {
                    PermissionResponse permissionResponse = new PermissionResponse();
                    permissionResponse.setName(p.getName());
                    permissionResponse.setDescription(p.getDescription());
                    permissionResponse.setAction(p.getAction());
                    return permissionResponse;
                }).toList();
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH ROLE",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        roleResponses,
                        p -> List.of(
                                p.getName(),
                                p.getDescription(),
                                p.getAction() == 1 ? "Hoạt động" : "Không hoạt động",
                                p.getPermissions().stream().map(PermissionNoAction::getName).collect(Collectors.joining(", "))
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
                                    case 2, 3 -> {
                                        richText.applyFont(0, 11, boldFont);
                                        richText.applyFont(11, 13, redBoldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH PERMISSION",
                        "Danh sách permission",
                        headers1,
                        dropdowns1,
                        widths1,
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
    public void exportToExcel(RoleListRequest request, HttpServletResponse response) throws IOException {
        List<Long> permissionIds = Arrays.stream(request.getPermissions().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        Sort sort = Utils.createSort(request.getSortBy(), request.getSortType(), List.of("name", "action", "createdAt", "updatedAt", "createdBy", "updatedBy"),"createdAt");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);
        Page<Role> rolePage = roleRepository.findRolesWithAllPermissions(
                request.getName(),
                request.getAction(),
                permissionIds,
                pageable
        );
        List<RoleResponse> roleResponses = getListRole(rolePage);
        List<String> headers = List.of(
                "Tên * \n(Tối đa 100 kí tự)",
                "Mô tả \n(Tối đa 255 kí tự)",
                "Trạng thái * \n(Hoạt động hoặc Không hoạt động)",
                "Permission * \n(Phải chọn ít nhất 1 permission)"
        );
        Map<Integer, String[]> dropdowns = Map.of(
                2, new String[]{"Hoạt động", "Không hoạt động"}
        );
        Map<Integer, Integer> widths = Map.of(
                0, 8000,
                1, 12000,
                2, 10000,
                3, 12000
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
        List<PermissionResponse> permissionResponses = permissionRepository.findAllStatus1()
                .stream().map(p -> {
                    PermissionResponse permissionResponse = new PermissionResponse();
                    permissionResponse.setName(p.getName());
                    permissionResponse.setDescription(p.getDescription());
                    permissionResponse.setAction(p.getAction());
                    return permissionResponse;
                }).toList();
        List<UtilsExcel.ExcelSheetConfig<?>> sheets = List.of(
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH ROLE",
                        "Thông tin danh sách",
                        headers,
                        dropdowns,
                        widths,
                        roleResponses,
                        p -> List.of(
                                p.getName(),
                                p.getDescription(),
                                p.getAction() == 1 ? "Hoạt động" : "Không hoạt động",
                                p.getPermissions().stream().map(PermissionNoAction::getName).collect(Collectors.joining(", "))
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
                                    case 2, 3 -> {
                                        richText.applyFont(0, 11, boldFont);
                                        richText.applyFont(11, 13, redBoldFont);
                                        richText.applyFont(14, header.length(), italicFont);
                                    }
                                }
                                cells[i].setCellValue(richText);
                            }
                        }
                ),
                new UtilsExcel.ExcelSheetConfig<>(
                        "DANH SÁCH PERMISSION",
                        "Danh sách permission",
                        headers1,
                        dropdowns1,
                        widths1,
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
    @Transactional
    public void importFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Set<String> existingNames = new HashSet<>(roleRepository.findAllNames());
            Set<String> excelNames = new HashSet<>();
            List<Role> roles = new ArrayList<>();
            for(int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(UtilsExcel.isRowEmpty(row)) continue;
                RoleRequest request = new RoleRequest();
                request.setName(UtilsExcel.getCellValue(row.getCell(0)));
                request.setDescription(UtilsExcel.getCellValue(row.getCell(1)));
                String statusStr = UtilsExcel.getCellValue(row.getCell(2));
                Integer status = statusStr.isEmpty() ? null : statusStr.trim().equals("Hoạt động") ? 1 : 0;
                request.setAction(status);
                String permissionStr = UtilsExcel.getCellValue(row.getCell(3));
                List<String> permissionArr = Arrays.stream(permissionStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                Set<Permission> permissions = permissionRepository.findByNameIn(permissionArr);
                List<Long> changePermissions = permissions.stream()
                        .map(Permission::getId)
                        .toList();
                request.setPermissions(changePermissions);
                Set<ConstraintViolation<RoleRequest>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                if (!excelNames.add(request.getName())){
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                if (existingNames.contains(request.getName())){
                    throw new AppException(RoleErrorCode.ROLE_ERROR_FILE);
                }
                Role role = new Role();
                role.setName(request.getName());
                role.setDescription(request.getDescription());
                role.setAction(request.getAction());
                role.setPermissions(new HashSet<>(permissions));
                roles.add(role);
            }
            roleRepository.saveAll(roles);
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
        List<String> results = roleRepository.findNamesByKeyword(keyword);
        return results != null ? results : Collections.emptyList();
    }
}

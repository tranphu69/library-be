package com.example.library.utils;

import com.example.library.exception.AppException;
import com.example.library.exception.messageError.ErrorCode;
import com.example.library.exception.messageError.UserErrorCode;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public class Utils {
    public static Sort createSort(
            String sortBy,
            String sortType,
            List<String> allowedFields,
            String defaultField
    ) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = defaultField;
        }
        if (!allowedFields.contains(sortBy)) {
            sortBy = defaultField;
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortType)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    public static void checkDuplicate(String value, Set<String> existing, Set<String> excel) {
        if (value == null || value.trim().isEmpty()) return;
        if (!excel.add(value) || existing.contains(value)) {
            throw new AppException(ErrorCode.ERROR_FILE);
        }
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> java.math.BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
            default -> "";
        };
    }
}

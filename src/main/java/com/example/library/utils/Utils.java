package com.example.library.utils;

import org.springframework.data.domain.Sort;

import java.util.List;

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
}

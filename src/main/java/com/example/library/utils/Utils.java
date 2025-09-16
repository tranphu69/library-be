package com.example.library.utils;

import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static List<Long> convertToLongList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static Sort createSort(String sortBy, String sortType) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        if (!isValidSortField(sortBy)) {
            sortBy = "createdAt";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortType)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    public static Sort createSortUsername(String sortBy, String sortType) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        if (!isValidSortField(sortBy)) {
            sortBy = "createdAt";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortType)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    public static boolean isValidSortField(String sortBy) {
        List<String> allowedFields = List.of("username", "isActive");
        return allowedFields.contains(sortBy);
    }
}

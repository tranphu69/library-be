package com.example.library.dto.response.role;

import java.util.List;

public class RoleListResponse {
    private List<RoleResponse> data;
    private Integer currentPage;
    private Integer currentSize;
    private Integer totalPages;
    private Integer totalElements;

    public List<RoleResponse> getData() {
        return data;
    }

    public void setData(List<RoleResponse> data) {
        this.data = data;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(Integer currentSize) {
        this.currentSize = currentSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }
}

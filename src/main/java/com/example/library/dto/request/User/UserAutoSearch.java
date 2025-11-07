package com.example.library.dto.request.User;

import jakarta.validation.constraints.Size;

public class UserAutoSearch {
    @Size(max = 100, message = "USER_KEYWORD_EXSITED")
    private String keyword;
    private String type;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

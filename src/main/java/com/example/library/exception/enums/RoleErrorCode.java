package com.example.library.exception.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum RoleErrorCode implements BaseErrorCode {
    ROLE_NO_EXSITED(3010, "Role không tồn tại!", HttpStatus.BAD_REQUEST),
    ROLE_EXSITED(3011, "Tên Role đã tồn tại!", HttpStatus.BAD_REQUEST),
    ROLE_NAME_EXCEED(3012, "Trường name vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    ROLE_NAME_EMPTY(3013, "Trường name không được bỏ trống!", HttpStatus.BAD_REQUEST),
    ROLE_DESCRIPTION_EXCEED(3014, "Trường description vượt quá 255 kí tự!", HttpStatus.BAD_REQUEST),
    ROLE_ACTION_EMPTY(3015, "Trường action không được bỏ trống!", HttpStatus.BAD_REQUEST),
    ROLE_VALUE(3016, "Giá trị không hợp lệ!", HttpStatus.BAD_REQUEST),
    ROLE_ERROR_FILE(3017, "File lỗi!", HttpStatus.BAD_REQUEST),
    ROLE_NOT_READ_FILE(3018, "Không đọc được file!", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSION_EMPTY(3019, "Permission không được để trống!", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    RoleErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}

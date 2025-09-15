package com.example.library.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum PermissionErrorCode implements BaseErrorCode {
    PERMISSION_EXSITED(1001, "Permission đã tồn tại!",HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_REQUEST(1002, "Trường tên không được bỏ trống!", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_MAX_LENGTH(1003, "Trường tên không vượt quá 50 kí tự!", HttpStatus.BAD_REQUEST),
    PERMISSION_DESCRIPTION_MAX_LENGTH(1004, "Trường mô tả không vượt quá 255 kí tự!", HttpStatus.BAD_REQUEST),
    PERMISSION_STATUS_REQUEST(1005, "Trường trạng thái không được bỏ trống!", HttpStatus.BAD_REQUEST),
    PERMISSION_INVALID_STATUS(1006, "Giá trị trường trạng thái không hợp lệ!", HttpStatus.BAD_REQUEST)
    ;


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    PermissionErrorCode(int code, String message, HttpStatusCode statusCode) {
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

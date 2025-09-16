package com.example.library.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum RoleErrorCode implements BaseErrorCode {
    ROLE_NAME_REQUEST(1011, "Trường tên không được để trống!", HttpStatus.BAD_REQUEST),
    ROLE_NAME_MAX_LENGTH(1012, "Trường tên không được quá 50 kí tự!", HttpStatus.BAD_REQUEST),
    ROLE_DESCRIPTION_MAX_LENGTH(1013, "Trường mô tả không được quá 255 kí tự!", HttpStatus.BAD_REQUEST),
    ROLE_STATUS_REQUEST(1014, "Trường trạng thái không được để trống!", HttpStatus.BAD_REQUEST),
    ROLE_INVALID_STATUS(1015, "Giá trị trường trạng thái không hợp lệ!", HttpStatus.BAD_REQUEST),
    ROLE_STATUS_1(1016, "Với trạng thái hoạt động cần ít nhất một permission!", HttpStatus.BAD_REQUEST),
    ROLE_EXSITED(1017, "Role đã tồn tại!", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXSITED(1018, "Role không tồn tại!", HttpStatus.BAD_REQUEST),
    ROLE_ERROR_FILE(1019, "File tải lên bị lỗi!", HttpStatus.BAD_REQUEST),
    ROLE_NOT_READ_FILE(1020, "Không thể đọc được file!", HttpStatus.BAD_REQUEST),
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

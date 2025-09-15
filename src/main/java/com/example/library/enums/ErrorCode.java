package com.example.library.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode implements BaseErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1006, "You do not have permission", HttpStatus.FORBIDDEN),
    ERROR_FILE(1015, "Dữ liệu không đúng", HttpStatus.BAD_REQUEST),
    NOT_READ_FILE(1014, "Không thể đọc file Excel", HttpStatus.BAD_REQUEST),

    //permisson
    PERMISSION_EXSITED(1007, "Permission đã tồn tại", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_EXSITED(1011, "Permission không tồn tại", HttpStatus.BAD_REQUEST),

    //role
    ROLE_EXSITED(1017, "Role đã tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXSITED(1018, "Role không tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_STATUS_1(1019, "Với trạng thái hoạt động cần ít nhất một permission", HttpStatus.BAD_REQUEST),

    //user
    USER_NOT_EXSITED(1018, "User không tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_EXSITED(1026, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_EXSITED(1027, "Username đã tồn tại", HttpStatus.BAD_REQUEST),
    ERROR_TYPE(1026, "Trường tìm kiếm không hợp lệ", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
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

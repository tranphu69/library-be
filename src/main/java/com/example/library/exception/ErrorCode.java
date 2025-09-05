package com.example.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXSITED(1002, "User existed", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1004, "Username not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1006, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    NAME_MAX_LENGTH(1012, "Trường name tối đa 50 kí tự", HttpStatus.BAD_REQUEST),
    NAME_REQUEST(1009, "Trường name không được để trống", HttpStatus.BAD_REQUEST),
    DESCRIPTION_MAX_LENGTH(1013, "Trường description tối đa 255 kí tự", HttpStatus.BAD_REQUEST),
    STATUS_REQUEST(1010, "Trường status không được để trống", HttpStatus.BAD_REQUEST),
    INVALID(1013, "Giá trị không hợp lệ", HttpStatus.BAD_REQUEST),
    ERROR_FILE(1015, "Dữ liệu không đúng", HttpStatus.BAD_REQUEST),
    NOT_READ_FILE(1014, "Không thể đọc file Excel", HttpStatus.BAD_REQUEST),

    //permisson
    PERMISSION_EXSITED(1007, "Permission đã tồn tại", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_EXSITED(1011, "Permission không tồn tại", HttpStatus.BAD_REQUEST),

    //role
    ROLE_EXSITED(1017, "Role đã tồn tại", HttpStatus.BAD_REQUEST),
    PERMISSIONS_REQUEST(1016, "Phải chọn ít nhất một Permission", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXSITED(1018, "Role không tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_STATUS_1(1019, "Với trạng thái hoạt động cần ít nhất một permission", HttpStatus.BAD_REQUEST),

    //user
    USER_NOT_EXSITED(1018, "User không tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_EXSITED(1026, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_EXSITED(1027, "Username đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_REQUEST(1020, "Trường username không được để trống", HttpStatus.BAD_REQUEST),
    EMAIL_REQUEST(1021, "Trường email không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUEST(1022, "Trường password không được để trống", HttpStatus.BAD_REQUEST),
    USERNAME_MAX_LENGTH(1023, "Trường username tối đa 50 kí tự", HttpStatus.BAD_REQUEST),
    EMAIL_MAX_LENGTH(1024, "Trường email tối đa 50 kí tự", HttpStatus.BAD_REQUEST),
    PASSWORD_MIN_LENGTH(1025, "Trường password tối thiểu 8 kí tự", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}

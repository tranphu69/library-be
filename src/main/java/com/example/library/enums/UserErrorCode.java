package com.example.library.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum UserErrorCode implements BaseErrorCode {
    USER_USERNAME_REQUEST(1021, "Trường username không được để trống!", HttpStatus.BAD_REQUEST),
    USER_USERNAME_MAX_LENGTH(1022, "Trường username không được vượt quá 50 kí tự!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_REQUEST(1023, "Trường email không được để trống!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_MAX_LENGTH(1024, "Trường email không được vượt quá 50 kí tự!", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_REQUEST(1025, "Trường password không được để trống!", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_MIN_LENGTH(1026, "Trường password phải có ít nhất 8 kí tự!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_EXSITED(1027, "Email đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_USERNAME_EXSITED(1028, "Username đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXSITED(1029, "User không đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_INVALID(1030, "Giá trị không hợp lệ!", HttpStatus.BAD_REQUEST),
    USER_ERROR_FILE(1031, "File tải lên đang lỗi!", HttpStatus.BAD_REQUEST),
    USER_NOT_READ_FILE(1032, "Không thể đọc được file!", HttpStatus.BAD_REQUEST),
    USER_ERROR_TYPE(1033, "Trường tìm kiếm không hợp lệ", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    UserErrorCode(int code, String message, HttpStatusCode statusCode) {
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

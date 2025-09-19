package com.example.library.enums;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum AuthErrorCode implements BaseErrorCode {
    AUTH_ERROR_CODE(1034, "Thông tin đăng nhập không chính xác!",HttpStatus.BAD_REQUEST),
    AUTH_INVALID_TOKEN(1035, "Token không hợp lệ!",HttpStatus.BAD_REQUEST),
    AUTH_EXPIRED_TOKEN(1036, "Mã thông báo làm mới đã hết hạn!",HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    AuthErrorCode(int code, String message, HttpStatusCode statusCode) {
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

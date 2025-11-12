package com.example.library.exception.messageError;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode implements BaseErrorCode {
    // ==== System errors (5xxx) ====
    UNCATEGORIZED_EXCEPTION(5999, "Ngoại lệ chưa phân loại!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(5998, "Lỗi chưa phân loại!", HttpStatus.BAD_REQUEST),
    NOT_VALUE(5997, "Giá trị không hợp lệ!", HttpStatus.BAD_REQUEST),
    ERROR_FILE(5996, "Tài liệu lỗi!", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR(5000, "Lỗi máy chủ nội bộ!", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== Auth errors (2xxx) ====
    AUTH_UNAUTHORIZED(2001, "Bạn không được ủy quyền!", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN(2002, "Bạn không có quyền!", HttpStatus.FORBIDDEN),
    AUTH_TOKEN_EXPIRED(2003, "Mã thông báo truy cập đã hết hạn!", HttpStatus.UNAUTHORIZED),
    AUTH_ERROR_CODE(2004, "Thông tin đăng nhập không chính xác!",HttpStatus.BAD_REQUEST),
    AUTH_CHECK_PASSWORD(2005, "Hai mật khẩu không trùng khớp!", HttpStatus.BAD_REQUEST),
    AUTH_TOKEN_INVALID(2006, "Mã thông báo không hợp lệ!", HttpStatus.BAD_REQUEST),
    AUTH_WRONG_PASSWORD(2007, "Mật khẩu không chính xác!", HttpStatus.BAD_REQUEST),
    AUTH_SAME_PASSWORD(2008, "Mật khẩu mới không được trùng mật khẩu cũ!", HttpStatus.BAD_REQUEST),
    AUTH_NOT_CHECK_PASSWORD(2009, "Mật khẩu không trùng khớp!", HttpStatus.BAD_REQUEST)
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

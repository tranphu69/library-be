package com.example.library.exception.enums;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    // ==== System errors (5xxx) ====
    INVALID_KEY(5998, "Uncategorized error", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED_EXCEPTION(5999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== Validation errors (1xxx) ====

    // ==== Auth errors (2xxx) ====
    AUTH_UNAUTHORIZED(2001, "You are not authorized", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN(2002, "You do not have permission", HttpStatus.FORBIDDEN),
    AUTH_TOKEN_EXPIRED(2003, "Access token has expired", HttpStatus.UNAUTHORIZED),
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

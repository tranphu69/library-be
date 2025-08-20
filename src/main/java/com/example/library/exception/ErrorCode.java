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

    //permisson
    PERMISSION_EXSITED(1007, "permission existed", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_EXSITED(1011, "permission not existed", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_REQUEST(1009, "Name is not null", HttpStatus.BAD_REQUEST),
    PERMISSION_STATUS_REQUEST(1010, "Status is not null", HttpStatus.BAD_REQUEST)
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

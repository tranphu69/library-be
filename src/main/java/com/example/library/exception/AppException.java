package com.example.library.exception;

import com.example.library.exception.enums.ErrorCode;

public class AppException extends RuntimeException {
    private BaseErrorCode errorCode;

    public AppException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}

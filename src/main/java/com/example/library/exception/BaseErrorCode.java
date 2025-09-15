package com.example.library.exception;

import com.example.library.enums.ErrorCode;
import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {

    int getCode();
    String getMessage();
    HttpStatusCode getStatusCode();
}

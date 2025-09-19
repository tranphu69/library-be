package com.example.library.exception;

import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {
    int getCode();
    String getMessage();
    HttpStatusCode getStatusCode();
}

package com.example.library.exception;

import com.example.library.dto.response.ApiResponse;
import com.example.library.exception.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ApiResponse> buildResponse(ErrorCode code) {
        return buildResponse(code, code.getMessage());
    }

    private ResponseEntity<ApiResponse> buildResponse(ErrorCode code, String message) {
        ApiResponse res = new ApiResponse();
        res.setCode(code.getCode());
        res.setMessage(message);
        return ResponseEntity.status(code.getStatusCode()).body(res);
    }

    private String mapAttributes(String msg, Map<String, Object> attrs) {
        for (var e : attrs.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return msg;
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        return buildResponse(ex.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(ErrorCode.AUTH_UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getFieldError();
        String enumKey = fieldError != null ? fieldError.getDefaultMessage() : null;
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attrs = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var error = ex.getBindingResult().getAllErrors().get(0);
            if (error.contains(ConstraintViolation.class)) {
                var violation = error.unwrap(ConstraintViolation.class);
                attrs = violation.getConstraintDescriptor().getAttributes();
            }
        } catch (IllegalArgumentException ignored) { }
        String message = (attrs != null) ? mapAttributes(errorCode.getMessage(), attrs) : errorCode.getMessage();
        return buildResponse(errorCode, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUncategorized(Exception ex) {
        return buildResponse(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }
}

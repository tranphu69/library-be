package com.example.library.exception;

import com.example.library.dto.response.ApiResponse;
import com.example.library.exception.messageError.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private ErrorCodeRegistry errorCodeRegistry;

    private ResponseEntity<ApiResponse> buildResponse(BaseErrorCode code) {
        return buildResponse(code, code.getMessage());
    }

    private ResponseEntity<ApiResponse> buildResponse(BaseErrorCode code, String message) {
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
        BaseErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map attrs = null;
        try {
            BaseErrorCode foundCode = errorCodeRegistry.find(enumKey);
            if (foundCode != null) {
                errorCode = foundCode;
            } else {
                errorCode = ErrorCode.valueOf(enumKey);
            }
            var errors = ex.getBindingResult().getAllErrors();
            if (!errors.isEmpty()) {
                var error = errors.get(0);
                if (error.contains(ConstraintViolation.class)) {
                    var violation = error.unwrap(ConstraintViolation.class);
                    attrs = violation.getConstraintDescriptor().getAttributes();
                }
            }
        } catch (IllegalArgumentException ignored) { }
        String message = (attrs == null) ? errorCode.getMessage() : mapAttributes(errorCode.getMessage(), attrs);
        return buildResponse(errorCode, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
        BaseErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attrs = null;
        String enumKey = null;
        try {
            ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
            enumKey = violation.getMessage();
            attrs = violation.getConstraintDescriptor().getAttributes();
            BaseErrorCode foundCode = errorCodeRegistry.find(enumKey);
            if (foundCode != null) {
                errorCode = foundCode;
            } else {
                errorCode = ErrorCode.valueOf(enumKey);
            }
        } catch (Exception e) {}
        String message = (attrs == null) ? errorCode.getMessage() : mapAttributes(errorCode.getMessage(), attrs);
        return buildResponse(errorCode, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUncategorized(Exception ex) {
        return buildResponse(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }
}
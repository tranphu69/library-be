package com.example.library.exception.messageError;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum PermissionErrorCode implements BaseErrorCode {
    PERMISSION_NO_EXSITED(3000, "Permission không tồn tại!", HttpStatus.BAD_REQUEST),
    PERMISSION_EXSITED(3001, "Tên Permission đã tồn tại!", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_EXCEED(3002, "Trường name vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_EMPTY(3003, "Trường name không được bỏ trống!", HttpStatus.BAD_REQUEST),
    PERMISSION_DESCRIPTION_EXCEED(3004, "Trường description vượt quá 255 kí tự!", HttpStatus.BAD_REQUEST),
    PERMISSION_ACTION_EMPTY(3005, "Trường action không được bỏ trống!", HttpStatus.BAD_REQUEST),
    PERMISSION_VALUE(3006, "Giá trị không hợp lệ!", HttpStatus.BAD_REQUEST),
    PERMISSION_ERROR_FILE(3007, "File lỗi!", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_READ_FILE(3008, "Không đọc được file!", HttpStatus.BAD_REQUEST),
    PERMISSION_IN_USE_BY_ROLE(3009, "Permission đang được dùng trong Role nên không được thay đổi trạng thái!", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    PermissionErrorCode(int code, String message, HttpStatusCode statusCode) {
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

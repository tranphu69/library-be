package com.example.library.exception.messageError;

import com.example.library.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum UserErrorCode implements BaseErrorCode {
    USER_USERNAME_EMPTY(3020, "Trường username không được để trống!", HttpStatus.BAD_REQUEST),
    USER_USERNAME_EXCEED(3021, "Trường username vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_EMPTY(3022, "Trường email không được để trống!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_EXCEED(3023, "Trường email vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_NOT_EMAIL(3024, "Trường email không đúng định dạng!", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_EMPTY(3025, "Trường password không được để trống!", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_EXCEED(3026, "Trường password có độ dài từ 10 đến 16 kí tự!", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_CHARACTER(3027, "Mật khẩu phải có ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt!", HttpStatus.BAD_REQUEST),
    USER_FULLNAME_EXCEED(3028, "Trường fullname vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_CODE_EXCEED(3029, "Trường code vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_CODE_COURSE(3030, "Trường course vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_PHONE_NUMBER(3031, "Trường phone chỉ được nhập số!", HttpStatus.BAD_REQUEST),
    USER_CODE_MAJOR(3032, "Trường major vượt quá 100 kí tự!", HttpStatus.BAD_REQUEST),
    USER_ROLE_EMPTY(3033, "Role không được để trống!", HttpStatus.BAD_REQUEST),
    USER_USERNAME_EXSITED(3034, "Username đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_EMAIL_EXSITED(3035, "Email đã tồn tại!", HttpStatus.BAD_REQUEST),
    USER_VALUE(3036, "Giá trị không hợp lệ!", HttpStatus.BAD_REQUEST),
    USER_STATUS_EMPTY(3037, "Trường status không được để trống!", HttpStatus.BAD_REQUEST),
    USER_NO_EXSITED(3038, "User không tồn tại!", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    UserErrorCode(int code, String message, HttpStatusCode statusCode) {
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

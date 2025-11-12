package com.example.library.dto.request;

import com.example.library.validation.OnCreate;
import com.example.library.validation.OnSignUp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message = "USER_PASSWORD_EMPTY", groups = {OnCreate.class, OnSignUp.class})
    private String oldPassword;
    @NotBlank(message = "USER_PASSWORD_EMPTY", groups = {OnCreate.class, OnSignUp.class})
    @Size(min = 10, max = 16, message = "USER_PASSWORD_EXCEED", groups = {OnCreate.class, OnSignUp.class})
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$",
            message = "USER_PASSWORD_CHARACTER",
            groups = {OnCreate.class, OnSignUp.class}
    )
    private String newPassword;
    @NotBlank(message = "USER_PASSWORD_EMPTY", groups = {OnCreate.class, OnSignUp.class})
    private String confirmPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

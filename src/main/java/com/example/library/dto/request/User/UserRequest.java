package com.example.library.dto.request.User;

import com.example.library.enums.AccountStatus;
import com.example.library.enums.Gender;
import com.example.library.enums.Position;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class UserRequest {
    @NotBlank(message = "USER_USERNAME_EMPTY")
    @Size(max = 100, message = "USER_USERNAME_EXCEED")
    private String username;
    @NotBlank(message = "USER_EMAIL_EMPTY")
    @Size(max = 100, message = "USER_EMAIL_EXCEED")
    @Email(message = "USER_NOT_EMAIL")
    private String email;
    @NotBlank(message = "USER_PASSWORD_EMPTY")
    @Size(min = 10, max = 16, message = "USER_PASSWORD_EXCEED")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$",
            message = "USER_PASSWORD_CHARACTER"
    )
    private String password;
    @Size(max = 100, message = "USER_FULLNAME_EXCEED")
    private String fullName;
    @Size(max = 100, message = "USER_CODE_EXCEED")
    private String code;
    @Pattern(regexp = "^[0-9]{10,11}$", message = "USER_PHONE_NUMBER")
    private String phone;
    @Size(max = 100, message = "USER_CODE_MAJOR")
    private String major;
    @Size(max = 100, message = "USER_CODE_COURSE")
    private String course;
    private String avatarUrl;
    private Position position;
    private Gender gender;
    private LocalDate dob;
    private AccountStatus status;
    private Boolean twoFactorEnabled;
    @NotNull(message = "USER_ROLE_EMPTY")
    @Size(min = 1, message = "USER_ROLE_EMPTY")
    private List<Long> roles;

    public @NotBlank(message = "USER_USERNAME_EMPTY") @Size(max = 100, message = "USER_USERNAME_EXCEED") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "USER_USERNAME_EMPTY") @Size(max = 100, message = "USER_USERNAME_EXCEED") String username) {
        this.username = username;
    }

    public @NotBlank(message = "USER_EMAIL_EMPTY") @Size(max = 100, message = "USER_EMAIL_EXCEED") @Email(message = "USER_NOT_EMAIL") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "USER_EMAIL_EMPTY") @Size(max = 100, message = "USER_EMAIL_EXCEED") @Email(message = "USER_NOT_EMAIL") String email) {
        this.email = email;
    }

    public @NotBlank(message = "USER_PASSWORD_EMPTY") @Size(min = 10, max = 16, message = "USER_PASSWORD_EXCEED") @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$",
            message = "USER_PASSWORD_CHARACTER"
    ) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "USER_PASSWORD_EMPTY") @Size(min = 10, max = 16, message = "USER_PASSWORD_EXCEED") @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,16}$",
            message = "USER_PASSWORD_CHARACTER"
    ) String password) {
        this.password = password;
    }

    public @Size(max = 100, message = "USER_FULLNAME_EXCEED") String getFullName() {
        return fullName;
    }

    public void setFullName(@Size(max = 100, message = "USER_FULLNAME_EXCEED") String fullName) {
        this.fullName = fullName;
    }

    public @Size(max = 100, message = "USER_CODE_EXCEED") String getCode() {
        return code;
    }

    public void setCode(@Size(max = 100, message = "USER_CODE_EXCEED") String code) {
        this.code = code;
    }

    public @Pattern(regexp = "^[0-9]{10,11}$", message = "USER_PHONE_NUMBER") String getPhone() {
        return phone;
    }

    public void setPhone(@Pattern(regexp = "^[0-9]{10,11}$", message = "USER_PHONE_NUMBER") String phone) {
        this.phone = phone;
    }

    public @Size(max = 100, message = "USER_CODE_MAJOR") String getMajor() {
        return major;
    }

    public void setMajor(@Size(max = 100, message = "USER_CODE_MAJOR") String major) {
        this.major = major;
    }

    public @Size(max = 100, message = "USER_CODE_COURSE") String getCourse() {
        return course;
    }

    public void setCourse(@Size(max = 100, message = "USER_CODE_COURSE") String course) {
        this.course = course;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public @NotNull(message = "USER_ROLE_EMPTY") @Size(min = 1, message = "USER_ROLE_EMPTY") List<Long> getRoles() {
        return roles;
    }

    public void setRoles(@NotNull(message = "USER_ROLE_EMPTY") @Size(min = 1, message = "USER_ROLE_EMPTY") List<Long> roles) {
        this.roles = roles;
    }
}

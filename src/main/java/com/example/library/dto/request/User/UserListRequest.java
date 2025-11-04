package com.example.library.dto.request.User;

import com.example.library.enums.AccountStatus;
import com.example.library.enums.Gender;
import com.example.library.enums.Position;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserListRequest {
    @Size(max = 100, message = "USER_USERNAME_EXCEED")
    private String username;
    @Size(max = 100, message = "USER_EMAIL_EXCEED")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "USER_NOT_EMAIL"
    )
    private String email;
    @Size(max = 100, message = "USER_FULLNAME_EXCEED")
    private String fullName;
    @Size(max = 100, message = "USER_CODE_EXCEED")
    private String code;
    @Size(max = 11, message = "USER_PHONE_NUMBER")
    private String phone;
    @Size(max = 100, message = "USER_CODE_MAJOR")
    private String major;
    @Size(max = 100, message = "USER_CODE_COURSE")
    private String course;
    private Position position;
    private Gender gender;
    private LocalDate dob;
    private AccountStatus status;
    private String roles;
    @Min(value = 0, message = "USER_VALUE")
    private Integer page;
    @Min(value = 1, message = "USER_VALUE")
    private Integer size;
    private String sortBy;
    @Pattern(regexp = "^(ASC|DESC)$", message = "USER_VALUE")
    private String sortType;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }
}

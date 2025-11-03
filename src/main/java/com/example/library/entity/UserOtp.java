package com.example.library.entity;

import com.example.library.enums.OtpPurpose;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otps")
public class UserOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(length = 6, nullable = false)
    private String code;
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // thời gian hết hạn
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose; // mục đích sử dụng otp
    @Column(nullable = false)
    private Boolean used = false; // trạng thái đã sử dụng
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UserOtp() {
    }

    public UserOtp(Long id, User user, String code, LocalDateTime expiredAt, OtpPurpose purpose, Boolean used, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.code = code;
        this.expiredAt = expiredAt;
        this.purpose = purpose;
        this.used = used;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

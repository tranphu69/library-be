package com.example.library.entity;

import com.example.library.enums.TypeToken;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_token")
    private TypeToken typeToken;

    public VerificationToken() {
    }

    public VerificationToken(Long id, String token, LocalDateTime expiryDate, User user, TypeToken typeToken) {
        this.id = id;
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
        this.typeToken = typeToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TypeToken getTypeToken() {
        return typeToken;
    }

    public void setTypeToken(TypeToken typeToken) {
        this.typeToken = typeToken;
    }
}

package com.example.library.repository;

import com.example.library.entity.RefreshToken;
import com.example.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    void deleteByToken(String token);
    void deleteByUserId(String userId);
}

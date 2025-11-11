package com.example.library.repository;

import com.example.library.entity.User;
import com.example.library.entity.VerificationToken;
import com.example.library.enums.TypeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    void deleteByUserAndTypeToken(User user, TypeToken typeToken);
    Optional<VerificationToken> findByTokenAndTypeToken(String token, TypeToken typeToken);
}

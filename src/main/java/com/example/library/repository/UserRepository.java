package com.example.library.repository;

import com.example.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByCode(String code);
    boolean existsByUsernameAndIdNot(String username, String id);
    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByPhoneAndIdNot(String phone, String id);
    boolean existsByCodeAndIdNot(String code, String id);
}

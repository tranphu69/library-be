package com.example.library.repository;

import com.example.library.entity.Role;
import com.example.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUsernameAndIdNot(String username, Long id);

    @Query("""
    SELECT u FROM User u
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    AND r.status <> -1
""")
    Page<User> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

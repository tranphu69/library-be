package com.example.library.repository;

import com.example.library.entity.User;
import com.example.library.enums.AccountStatus;
import com.example.library.enums.Gender;
import com.example.library.enums.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    @Query("""
    SELECT u FROM User u
    JOIN u.roles r
    WHERE u.status <> "DELETED"
        AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
        AND (:code IS NULL OR LOWER(u.code) LIKE LOWER(CONCAT('%', :code, '%')))
        AND (:phone IS NULL OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%')))
        AND (:major IS NULL OR LOWER(u.major) LIKE LOWER(CONCAT('%', :major, '%')))
        AND (:course IS NULL OR LOWER(u.course) LIKE LOWER(CONCAT('%', :course, '%')))
        AND (:dob IS NULL OR u.dob = :dob)
        AND (:position IS NULL OR u.position = :position)
        AND (:gender IS NULL OR u.gender = :gender)
        AND (:status IS NULL OR u.status = :status)
        AND (
            :roles IS NULL
            OR :#{#roles.isEmpty()} = TRUE
            OR r.id IN :roles
        )
    GROUP BY u.id
    HAVING (:roles IS NULL OR :#{#roles.isEmpty()} = TRUE OR COUNT(DISTINCT r.id) = :#{#roles.size()})
""")
    Page<User> findUsersWithAllRoles(
            @Param("username") String username,
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("code") String code,
            @Param("phone") String phone,
            @Param("major") String major,
            @Param("course") String course,
            @Param("position") Position position,
            @Param("gender") Gender gender,
            @Param("dob") LocalDate dob,
            @Param("status") AccountStatus status,
            @Param("roles") List<Long> roles,
            Pageable pageable
    );
}

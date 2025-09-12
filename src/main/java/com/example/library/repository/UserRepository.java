package com.example.library.repository;

import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import com.example.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByUsernameAndIdNot(String username, Long id);

    List<User> findByEmailStartingWithIgnoreCase(String email);
    List<User> findByUsernameStartingWithIgnoreCase(String username);

    @Query("""
SELECT u FROM User u
WHERE u.isActive <> -1 
AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
AND (:isActive IS NULL OR u.isActive = :isActive)
AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
AND (:roles IS NULL OR (
    SELECT COUNT(DISTINCT r.id)
    FROM u.roles r
    WHERE r.status = 1 AND r.id IN :roles
) = :#{#roles.size()})
""")
    Page<User> findUsersWithFilter(
            @Param("email") String email,
            @Param("isActive") Integer isActive,
            @Param("username") String username,
            @Param("roles") List<Long> roles,
            Pageable pageable
    );
}

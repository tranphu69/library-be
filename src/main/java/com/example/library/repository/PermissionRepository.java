package com.example.library.repository;

import com.example.library.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
    boolean existsById(Long id);

    @Query("SELECT p FROM Permission p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Permission> findPermissionsWithFilters(
            @Param("name") String name,
            @Param("status") Integer status,
            Pageable pageable
    );

    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Permission> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

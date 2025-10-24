package com.example.library.repository;

import com.example.library.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
    boolean existsById(Long id);
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT p FROM Permission p WHERE p.action <> -1 AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:action IS NULL OR p.action = :action)")
    Page<Permission> findPermissionsWithFilters(
            @Param("name") String name,
            @Param("action") Integer status,
            Pageable pageable
    );

    @Query("SELECT p.name FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<String> findNamesByKeyword(@Param("keyword") String keyword);
}

package com.example.library.repository;

import com.example.library.entity.Permission;
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
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);
    boolean existsById(Long id);
    List<Permission> findAllByIdIn(List<Long> ids);

    @Query("SELECT p FROM Permission p WHERE p.status <> -1 AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Permission> findPermissionsWithFilters(
            @Param("name") String name,
            @Param("status") Integer status,
            Pageable pageable
    );

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.status = 1")
    List<Permission> findAllActiveById(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Permission p WHERE p.status = 1")
    List<Permission> findAllStatus1();

    Set<Permission> findByNameIn(List<String> names);

    boolean existsByNameAndId(String name, Long id);
    List<Permission> findByNameStartingWithIgnoreCase(String name);
}

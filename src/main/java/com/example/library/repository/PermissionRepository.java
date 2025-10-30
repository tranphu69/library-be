package com.example.library.repository;

import com.example.library.entity.Permission;
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
    boolean existsByNameAndIdNot(String name, Long id);
    Set<Permission> findByNameIn(List<String> names);

    @Query("SELECT p FROM Permission p WHERE p.action <> -1 AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:action IS NULL OR p.action = :action)")
    Page<Permission> findPermissionsWithFilters(
            @Param("name") String name,
            @Param("action") Integer action,
            Pageable pageable
    );

    @Query("SELECT p.name FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<String> findNamesByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p.name FROM Permission p WHERE p.action <> -1")
    List<String> findAllNames();

    @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.action = 1")
    List<Permission> findAllActiveById(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Permission p WHERE p.action = 1")
    List<Permission> findAllStatus1();
}

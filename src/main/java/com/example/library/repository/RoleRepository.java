package com.example.library.repository;

import com.example.library.entity.Permission;
import com.example.library.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(String name);
    boolean existsById(Long id);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByPermissions_Id(Long permissionId);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.id IN :permissionIds")
    List<Role> findAllByPermissionIds(@Param("permissionIds") List<Long> permissionIds);

    @Query("SELECT r FROM Role r WHERE r.action <> -1 AND " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:action IS NULL OR r.action = :action)")
    Page<Role> findRolesWithFilters(
            @Param("name") String name,
            @Param("action") Integer action,
            Pageable pageable
    );
}

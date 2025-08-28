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

    @Query("""
SELECT r FROM Role r
WHERE r.status <> -1
AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))
AND (:status IS NULL OR r.status = :status)
AND (:permissions IS NULL OR (
    SELECT COUNT(DISTINCT p.id)
    FROM r.permissions p
    WHERE p.status = 1 AND p.id IN :permissions
) = :#{#permissions.size()})
""")
    Page<Role> findRolesWithFilter(
            @Param("name") String name,
            @Param("status") Integer status,
            @Param("permissions") List<Long> permissions,
            Pageable pageable
    );

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.id IN :permissionIds")
    List<Role> findDistinctByPermissions_IdIn(@Param("permissionIds") List<Long> permissionIds);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findDistinctByPermissions_Id(@Param("permissionId") Long permissionId);

    @Modifying
    @Query("UPDATE Role r SET r.status = 0 WHERE r.id IN :ids")
    void deactivateRoles(@Param("ids") List<Long> ids);

    @Query("""
    SELECT r FROM Role r
    WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    AND r.status <> -1
""")
    Page<Role> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

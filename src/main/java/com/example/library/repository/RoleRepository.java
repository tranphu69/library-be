package com.example.library.repository;

import com.example.library.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("""
    SELECT r FROM Role r
    JOIN r.permissions p
    WHERE r.action <> -1
      AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:action IS NULL OR r.action = :action)
      AND (
        :permissions IS NULL\s
        OR :#{#permissions.isEmpty()} = TRUE
        OR p.id IN :permissions
      )
    GROUP BY r.id
    HAVING (:permissions IS NULL OR :#{#permissions.isEmpty()} = TRUE OR COUNT(DISTINCT p.id) = :#{#permissions.size()})
""")
    Page<Role> findRolesWithAllPermissions(
            @Param("name") String name,
            @Param("action") Integer action,
            @Param("permissions") List<Long> permissions,
            Pageable pageable
    );

    @Query("SELECT r.name FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<String> findNamesByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r.name FROM Role r WHERE r.action <> -1")
    List<String> findAllNames();

    @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.action = 1")
    List<Role> findAllActiveById(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM Role r WHERE r.action = 1")
    List<Role> findAllStatus1();
}

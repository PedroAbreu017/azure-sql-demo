// RoleRepository.java - CORRIGIDO
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name (String, not enum)
     */
    Optional<Role> findByName(String name);

    /**
     * Find role by name ignoring case
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if role exists by name ignoring case
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all active roles
     */
    List<Role> findByIsActiveTrue();

    /**
     * Find all inactive roles
     */
    List<Role> findByIsActiveFalse();

    /**
     * Find roles by user ID
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * Count users by role name
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countUsersByRoleName(@Param("roleName") String roleName);

    /**
     * Find default role (USER role)
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'USER' AND r.isActive = true")
    Optional<Role> findDefaultRole();

    /**
     * Find admin role
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'ADMIN' AND r.isActive = true")
    Optional<Role> findAdminRole();

    /**
     * Find roles by names
     */
    List<Role> findByNameIn(List<String> names);

    /**
     * Find roles with user count
     */
    @Query("SELECT r, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r")
    List<Object[]> findRolesWithUserCount();
}
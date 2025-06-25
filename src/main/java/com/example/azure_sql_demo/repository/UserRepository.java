// UserRepository.java - CORRIGIDO
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists excluding specific user ID
     */
    boolean existsByUsernameAndIdNot(String username, Long id);

    /**
     * Check if email exists excluding specific user ID
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Find enabled users only
     */
    List<User> findByIsEnabledTrue();

    /**
     * Find disabled users
     */
    List<User> findByIsEnabledFalse();

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find users with failed login attempts above threshold
     */
    List<User> findByFailedLoginAttemptsGreaterThan(Integer threshold);

    /**
     * Find users who logged in recently
     */
    List<User> findByLastLoginAfter(LocalDateTime since);

    /**
     * Find users created after date
     */
    List<User> findByCreatedAtAfter(LocalDateTime since);

    /**
     * Search users by name or username
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Find locked users
     */
    List<User> findByIsAccountNonLockedFalse();

    /**
     * Find users with expired credentials
     */
    List<User> findByIsCredentialsNonExpiredFalse();

    /**
     * Find users with expired accounts
     */
    List<User> findByIsAccountNonExpiredFalse();
}
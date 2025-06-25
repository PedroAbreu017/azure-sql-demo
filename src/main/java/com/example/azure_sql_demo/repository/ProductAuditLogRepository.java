// ProductAuditLogRepository.java
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.ProductAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductAuditLogRepository extends JpaRepository<ProductAuditLog, Long> {

    /**
     * Find audit logs by product ID
     */
    List<ProductAuditLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * Find audit logs by product ID with pagination
     */
    Page<ProductAuditLog> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    List<ProductAuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs by user ID
     */
    List<ProductAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find audit logs by date range
     */
    List<ProductAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent audit logs
     */
    List<ProductAuditLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find audit logs by product and action
     */
    List<ProductAuditLog> findByProductIdAndActionOrderByCreatedAtDesc(Long productId, String action);

    /**
     * Count audit logs by product
     */
    Long countByProductId(Long productId);

    /**
     * Count audit logs by action
     */
    Long countByAction(String action);

    /**
     * Find audit logs by multiple product IDs
     */
    List<ProductAuditLog> findByProductIdInOrderByCreatedAtDesc(List<Long> productIds);

    /**
     * Find audit logs with custom query
     */
    @Query("SELECT p FROM ProductAuditLog p WHERE " +
           "(:productId IS NULL OR p.productId = :productId) AND " +
           "(:action IS NULL OR p.action = :action) AND " +
           "(:userId IS NULL OR p.userId = :userId) AND " +
           "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR p.createdAt <= :endDate) " +
           "ORDER BY p.createdAt DESC")
    Page<ProductAuditLog> findAuditLogsByCriteria(
            @Param("productId") Long productId,
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get audit statistics
     */
    @Query("SELECT p.action, COUNT(p) FROM ProductAuditLog p GROUP BY p.action")
    List<Object[]> getAuditStatsByAction();

    /**
     * Find recent changes by user
     */
    @Query("SELECT p FROM ProductAuditLog p WHERE p.userId = :userId AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<ProductAuditLog> findRecentChangesByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}

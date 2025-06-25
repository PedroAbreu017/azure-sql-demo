// FinancialAuditLogRepository.java
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.FinancialAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialAuditLogRepository extends JpaRepository<FinancialAuditLog, Long> {

    /**
     * Find audit logs by account ID
     */
    List<FinancialAuditLog> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    /**
     * Find audit logs by account ID with pagination
     */
    Page<FinancialAuditLog> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    /**
     * Find audit logs by account number
     */
    List<FinancialAuditLog> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    /**
     * Find audit logs by transaction ID
     */
    List<FinancialAuditLog> findByTransactionIdOrderByCreatedAtDesc(Long transactionId);

    /**
     * Find audit logs by action
     */
    List<FinancialAuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs by user ID
     */
    List<FinancialAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find audit logs by date range
     */
    List<FinancialAuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent audit logs
     */
    List<FinancialAuditLog> findTop20ByOrderByCreatedAtDesc();

    /**
     * Find audit logs by amount range
     */
    List<FinancialAuditLog> findByAmountBetweenOrderByCreatedAtDesc(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find audit logs by amount greater than
     */
    List<FinancialAuditLog> findByAmountGreaterThanOrderByCreatedAtDesc(BigDecimal amount);

    /**
     * Count audit logs by account
     */
    Long countByAccountId(Long accountId);

    /**
     * Count audit logs by action
     */
    Long countByAction(String action);

    /**
     * Sum amounts by action
     */
    @Query("SELECT SUM(f.amount) FROM FinancialAuditLog f WHERE f.action = :action AND f.amount IS NOT NULL")
    BigDecimal sumAmountsByAction(@Param("action") String action);

    /**
     * Find high-value transactions
     */
    @Query("SELECT f FROM FinancialAuditLog f WHERE f.amount >= :threshold ORDER BY f.amount DESC, f.createdAt DESC")
    List<FinancialAuditLog> findHighValueTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Find audit logs with custom criteria
     */
    @Query("SELECT f FROM FinancialAuditLog f WHERE " +
           "(:accountId IS NULL OR f.accountId = :accountId) AND " +
           "(:transactionId IS NULL OR f.transactionId = :transactionId) AND " +
           "(:action IS NULL OR f.action = :action) AND " +
           "(:userId IS NULL OR f.userId = :userId) AND " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
           "(:minAmount IS NULL OR f.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR f.amount <= :maxAmount) " +
           "ORDER BY f.createdAt DESC")
    Page<FinancialAuditLog> findAuditLogsByCriteria(
            @Param("accountId") Long accountId,
            @Param("transactionId") Long transactionId,
            @Param("action") String action,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * Get audit statistics by action
     */
    @Query("SELECT f.action, COUNT(f), COALESCE(SUM(f.amount), 0) FROM FinancialAuditLog f GROUP BY f.action")
    List<Object[]> getAuditStatsByAction();

    /**
     * Get daily transaction summary
     */
    @Query("SELECT DATE(f.createdAt), f.action, COUNT(f), COALESCE(SUM(f.amount), 0) " +
           "FROM FinancialAuditLog f " +
           "WHERE f.createdAt >= :startDate " +
           "GROUP BY DATE(f.createdAt), f.action " +
           "ORDER BY DATE(f.createdAt) DESC, f.action")
    List<Object[]> getDailyTransactionSummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Find suspicious activities (high frequency or high amounts)
     */
    @Query("SELECT f.accountId, f.action, COUNT(f) as frequency, MAX(f.amount) as maxAmount " +
           "FROM FinancialAuditLog f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY f.accountId, f.action " +
           "HAVING COUNT(f) > :frequencyThreshold OR MAX(f.amount) > :amountThreshold " +
           "ORDER BY frequency DESC, maxAmount DESC")
    List<Object[]> findSuspiciousActivities(
            @Param("since") LocalDateTime since,
            @Param("frequencyThreshold") Long frequencyThreshold,
            @Param("amountThreshold") BigDecimal amountThreshold);

    /**
     * Find user activity summary
     */
    @Query("SELECT f.userId, COUNT(f), COALESCE(SUM(f.amount), 0) " +
           "FROM FinancialAuditLog f " +
           "WHERE f.createdAt >= :startDate " +
           "GROUP BY f.userId " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getUserActivitySummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Find account balance changes over time
     */
    @Query("SELECT f FROM FinancialAuditLog f " +
           "WHERE f.accountId = :accountId " +
           "AND f.action IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') " +
           "ORDER BY f.createdAt ASC")
    List<FinancialAuditLog> findAccountBalanceHistory(@Param("accountId") Long accountId);
}
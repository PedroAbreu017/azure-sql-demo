// FinancialTransactionRepository.java - COMPLETO
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.Account;
import com.example.azure_sql_demo.model.FinancialTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    // ========== BASIC QUERIES ==========

    /**
     * Find transactions by from account or to account
     */
    Page<FinancialTransaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(
            Account fromAccount, Account toAccount, Pageable pageable);

    /**
     * Find transactions by user ID (from or to account)
     */
    Page<FinancialTransaction> findByFromAccount_UserIdOrToAccount_UserIdOrderByCreatedAtDesc(
            Long fromUserId, Long toUserId, Pageable pageable);

    /**
     * Find transactions by from account
     */
    List<FinancialTransaction> findByFromAccountOrderByCreatedAtDesc(Account fromAccount);

    /**
     * Find transactions by to account
     */
    List<FinancialTransaction> findByToAccountOrderByCreatedAtDesc(Account toAccount);

    /**
     * Find transactions by from account with pagination
     */
    Page<FinancialTransaction> findByFromAccountOrderByCreatedAtDesc(Account fromAccount, Pageable pageable);

    /**
     * Find transactions by to account with pagination
     */
    Page<FinancialTransaction> findByToAccountOrderByCreatedAtDesc(Account toAccount, Pageable pageable);

    // ========== STATUS QUERIES ==========

    /**
     * Find transactions by status
     */
    List<FinancialTransaction> findByStatusOrderByCreatedAtDesc(FinancialTransaction.TransactionStatus status);

    /**
     * Find transactions by status with pagination
     */
    Page<FinancialTransaction> findByStatusOrderByCreatedAtDesc(
            FinancialTransaction.TransactionStatus status, Pageable pageable);

    /**
     * Find pending transactions
     */
    List<FinancialTransaction> findByStatusOrderByCreatedAtAsc(FinancialTransaction.TransactionStatus status);

    /**
     * Count transactions by status
     */
    Long countByStatus(FinancialTransaction.TransactionStatus status);

    // ========== TRANSACTION TYPE QUERIES ==========

    /**
     * Find transactions by type
     */
    List<FinancialTransaction> findByTransactionTypeOrderByCreatedAtDesc(
            FinancialTransaction.TransactionType transactionType);

    /**
     * Find transactions by type with pagination
     */
    Page<FinancialTransaction> findByTransactionTypeOrderByCreatedAtDesc(
            FinancialTransaction.TransactionType transactionType, Pageable pageable);

    /**
     * Count transactions by type
     */
    Long countByTransactionType(FinancialTransaction.TransactionType transactionType);

    // ========== REFERENCE NUMBER QUERIES ==========

    /**
     * Find transaction by reference number
     */
    Optional<FinancialTransaction> findByReferenceNumber(String referenceNumber);

    /**
     * Check if reference number exists
     */
    boolean existsByReferenceNumber(String referenceNumber);

    // ========== DATE RANGE QUERIES ==========

    /**
     * Find transactions by date range
     */
    List<FinancialTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find transactions by date range with pagination
     */
    Page<FinancialTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find transactions after date
     */
    List<FinancialTransaction> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    /**
     * Find transactions before date
     */
    List<FinancialTransaction> findByCreatedAtBeforeOrderByCreatedAtDesc(LocalDateTime date);

    // ========== AMOUNT QUERIES ==========

    /**
     * Find transactions by amount range
     */
    List<FinancialTransaction> findByAmountBetweenOrderByCreatedAtDesc(
            BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find transactions with amount greater than
     */
    List<FinancialTransaction> findByAmountGreaterThanOrderByAmountDesc(BigDecimal amount);

    /**
     * Find transactions with amount less than
     */
    List<FinancialTransaction> findByAmountLessThanOrderByAmountDesc(BigDecimal amount);

    // ========== USER SPECIFIC QUERIES ==========

    /**
     * Find user's transactions (as sender)
     */
    List<FinancialTransaction> findByFromAccount_UserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find user's transactions (as receiver)
     */
    List<FinancialTransaction> findByToAccount_UserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find user's transactions with pagination (as sender)
     */
    Page<FinancialTransaction> findByFromAccount_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find user's transactions with pagination (as receiver)
     */
    Page<FinancialTransaction> findByToAccount_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ========== COMPLEX QUERIES ==========

    /**
     * Find transactions with multiple criteria
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "(:userId IS NULL OR t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "ORDER BY t.createdAt DESC")
    Page<FinancialTransaction> findTransactionsByCriteria(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") FinancialTransaction.TransactionStatus status,
            @Param("transactionType") FinancialTransaction.TransactionType transactionType,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * Search transactions by description
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.referenceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.createdAt DESC")
    List<FinancialTransaction> searchTransactions(@Param("searchTerm") String searchTerm);

    /**
     * Search transactions by description with pagination
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.referenceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.createdAt DESC")
    Page<FinancialTransaction> searchTransactions(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ========== STATISTICS QUERIES ==========

    /**
     * Calculate total amount by transaction type
     */
    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE " +
           "t.transactionType = :transactionType AND t.status = 'COMPLETED'")
    Optional<BigDecimal> sumAmountByTransactionType(
            @Param("transactionType") FinancialTransaction.TransactionType transactionType);

    /**
     * Calculate total amount by user (sent)
     */
    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE " +
           "t.fromAccount.user.id = :userId AND t.status = 'COMPLETED'")
    Optional<BigDecimal> sumAmountSentByUser(@Param("userId") Long userId);

    /**
     * Calculate total amount by user (received)
     */
    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE " +
           "t.toAccount.user.id = :userId AND t.status = 'COMPLETED'")
    Optional<BigDecimal> sumAmountReceivedByUser(@Param("userId") Long userId);

    /**
     * Get transaction statistics by date range
     */
    @Query("SELECT t.transactionType, t.status, COUNT(t), COALESCE(SUM(t.amount), 0) " +
           "FROM FinancialTransaction t WHERE " +
           "t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionType, t.status " +
           "ORDER BY t.transactionType, t.status")
    List<Object[]> getTransactionStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ========== RECENT TRANSACTIONS ==========

    /**
     * Find recent transactions (last 24 hours)
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<FinancialTransaction> findRecentTransactions(@Param("since") LocalDateTime since);

    /**
     * Find recent transactions with limit
     */
    List<FinancialTransaction> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find recent user transactions
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "(t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId) AND " +
           "t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<FinancialTransaction> findRecentUserTransactions(
            @Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ========== ACCOUNT BALANCE QUERIES ==========

    /**
     * Calculate account balance based on transactions
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN t.toAccount.id = :accountId THEN t.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN t.fromAccount.id = :accountId THEN t.amount ELSE 0 END), 0) " +
           "FROM FinancialTransaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) AND " +
           "t.status = 'COMPLETED'")
    Optional<BigDecimal> calculateAccountBalanceFromTransactions(@Param("accountId") Long accountId);

    // ========== FAILED TRANSACTIONS ==========

    /**
     * Find failed transactions for retry
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "t.status = 'FAILED' AND t.createdAt >= :since " +
           "ORDER BY t.createdAt DESC")
    List<FinancialTransaction> findFailedTransactionsForRetry(@Param("since") LocalDateTime since);

    /**
     * Find transactions that need processing
     */
    List<FinancialTransaction> findByStatusInOrderByCreatedAtAsc(
            List<FinancialTransaction.TransactionStatus> statuses);

    // ========== DUPLICATE DETECTION ==========

    /**
     * Find potential duplicate transactions
     */
    @Query("SELECT t FROM FinancialTransaction t WHERE " +
           "t.fromAccount.id = :fromAccountId AND " +
           "t.toAccount.id = :toAccountId AND " +
           "t.amount = :amount AND " +
           "t.createdAt >= :since " +
           "ORDER BY t.createdAt DESC")
    List<FinancialTransaction> findPotentialDuplicates(
            @Param("fromAccountId") Long fromAccountId,
            @Param("toAccountId") Long toAccountId,
            @Param("amount") BigDecimal amount,
            @Param("since") LocalDateTime since);
}
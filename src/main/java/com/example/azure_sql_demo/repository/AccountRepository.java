// AccountRepository.java - CORRIGIDO
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find accounts by user ID
     */
    List<Account> findByUserId(Long userId);

    /**
     * Find active accounts by user ID
     */
    List<Account> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find accounts by account type
     */
    List<Account> findByAccountType(Account.AccountType accountType);

    /**
     * Find accounts by user ID and account type
     */
    List<Account> findByUserIdAndAccountType(Long userId, Account.AccountType accountType);

    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find active accounts only
     */
    List<Account> findByIsActiveTrue();

    /**
     * Find inactive accounts
     */
    List<Account> findByIsActiveFalse();

    /**
     * Find frozen accounts
     */
    List<Account> findByIsFrozenTrue();

    /**
     * Find accounts with balance greater than amount
     */
    List<Account> findByBalanceGreaterThan(BigDecimal amount);

    /**
     * Find accounts with balance less than amount
     */
    List<Account> findByBalanceLessThan(BigDecimal amount);

    /**
     * Find accounts with balance between amounts
     */
    List<Account> findByBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);

    /**
     * Find overdrawn accounts (negative balance)
     */
    @Query("SELECT a FROM Account a WHERE a.balance < 0 AND a.isActive = true")
    List<Account> findOverdrawnAccounts();

    /**
     * Calculate total balance for user
     */
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    Optional<BigDecimal> calculateTotalBalanceByUserId(@Param("userId") Long userId);

    /**
     * Count accounts by type
     */
    Long countByAccountType(Account.AccountType accountType);

    /**
     * Count active accounts by user
     */
    Long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find accounts by currency
     */
    List<Account> findByCurrency(String currency);

    /**
     * Find accounts by user and currency
     */
    List<Account> findByUserIdAndCurrency(Long userId, String currency);

    /**
     * Find accounts with credit limit
     */
    @Query("SELECT a FROM Account a WHERE a.creditLimit IS NOT NULL AND a.creditLimit > 0")
    List<Account> findAccountsWithCreditLimit();

    /**
     * Find accounts near credit limit
     */
    @Query("SELECT a FROM Account a WHERE a.creditLimit IS NOT NULL AND " +
           "a.balance < 0 AND ABS(a.balance) > (a.creditLimit * 0.9)")
    List<Account> findAccountsNearCreditLimit();
}
// AccountService.java - CORRIGIDO (método createAccount)
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.dto.AccountDTO;
import com.example.azure_sql_demo.dto.CreateAccountRequest;
import com.example.azure_sql_demo.exception.BusinessException;
import com.example.azure_sql_demo.mapper.AccountMapper;
import com.example.azure_sql_demo.model.Account;
import com.example.azure_sql_demo.model.User;
import com.example.azure_sql_demo.repository.AccountRepository;
import com.example.azure_sql_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final AuditService auditService;

    /**
     * Creates a new account for the current authenticated user
     */
    @Transactional
    public AccountDTO createAccount(CreateAccountRequest request) {
        log.info("Creating new account of type: {}", request.getAccountType());
        
        // Get current authenticated user
        User currentUser = getCurrentUser();
        
        // Validate business rules
        validateAccountCreation(request, currentUser);
        
        // Create account entity
        Account account = accountMapper.toEntity(request);
        account.setUser(currentUser); // CORRIGIDO: Set user manually
        account.setAccountNumber(generateAccountNumber());
        
        // Set initial balance from deposit
        if (request.getInitialDeposit() != null) {
            account.setBalance(request.getInitialDeposit());
        } else {
            account.setBalance(BigDecimal.ZERO);
        }
        
        // Save account
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with number: {}", savedAccount.getAccountNumber());
        
        // Audit log
        auditService.logAccountCreation(savedAccount);
        
        return accountMapper.toDTO(savedAccount);
    }

    /**
     * Creates a new account for a specific user (admin function)
     */
    @Transactional
    public AccountDTO createAccountForUser(CreateAccountRequest request, Long userId) {
        log.info("Creating new account for user: {} of type: {}", userId, request.getAccountType());
        
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found with id: " + userId));
        
        // Validate business rules
        validateAccountCreation(request, user);
        
        // Create account entity
        Account account = accountMapper.toEntity(request);
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        
        // Set initial balance
        if (request.getInitialDeposit() != null) {
            account.setBalance(request.getInitialDeposit());
        } else {
            account.setBalance(BigDecimal.ZERO);
        }
        
        // Save account
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully for user: {} with number: {}", 
                userId, savedAccount.getAccountNumber());
        
        // Audit log
        auditService.logAccountCreation(savedAccount);
        
        return accountMapper.toDTO(savedAccount);
    }

    /**
     * Get account by account number
     */
    public AccountDTO getAccountByNumber(String accountNumber) {
        log.info("Fetching account by number: {}", accountNumber);
        Account account = findAccountByNumber(accountNumber);
        return accountMapper.toDTO(account);
    }

    /**
     * Get account by ID
     */
    public AccountDTO getAccountById(Long id) {
        log.info("Fetching account by id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found with id: " + id));
        return accountMapper.toDTO(account);
    }

    /**
     * Get all accounts for current user
     */
    public List<AccountDTO> getCurrentUserAccounts() {
        User currentUser = getCurrentUser();
        log.info("Fetching accounts for user: {}", currentUser.getUsername());
        
        List<Account> accounts = accountRepository.findByUserIdAndIsActiveTrue(currentUser.getId());
        return accountMapper.toDTOList(accounts);
    }

    /**
     * Get all accounts for specific user
     */
    public List<AccountDTO> getUserAccounts(Long userId) {
        log.info("Fetching accounts for user id: {}", userId);
        List<Account> accounts = accountRepository.findByUserIdAndIsActiveTrue(userId);
        return accountMapper.toDTOList(accounts);
    }

    /**
     * Deposit money to account
     */
    @Transactional
    public AccountDTO deposit(String accountNumber, BigDecimal amount) {
        log.info("Depositing {} to account: {}", amount, accountNumber);
        
        validateDepositAmount(amount);
        Account account = findAccountByNumber(accountNumber);
        validateAccountForTransaction(account);
        
        BigDecimal oldBalance = account.getBalance();
        account.credit(amount);
        
        Account savedAccount = accountRepository.save(account);
        log.info("Deposit successful. Balance updated from {} to {}", oldBalance, savedAccount.getBalance());
        
        // Audit log
        auditService.logAccountDeposit(savedAccount, amount, oldBalance);
        
        return accountMapper.toDTO(savedAccount);
    }

    /**
     * Withdraw money from account
     */
    @Transactional
    public AccountDTO withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdrawing {} from account: {}", amount, accountNumber);
        
        validateWithdrawalAmount(amount);
        Account account = findAccountByNumber(accountNumber);
        validateAccountForTransaction(account);
        
        if (!account.canDebit(amount)) {
            throw new BusinessException("Insufficient funds for withdrawal");
        }
        
        BigDecimal oldBalance = account.getBalance();
        account.debit(amount);
        
        Account savedAccount = accountRepository.save(account);
        log.info("Withdrawal successful. Balance updated from {} to {}", oldBalance, savedAccount.getBalance());
        
        // Audit log
        auditService.logAccountWithdrawal(savedAccount, amount, oldBalance);
        
        return accountMapper.toDTO(savedAccount);
    }

    // ========== SECURITY METHODS ==========

    /**
     * Check if current user owns the account
     */
    public boolean isAccountOwner(String accountNumber, Long userId) {
        try {
            Account account = findAccountByNumber(accountNumber);
            return account.getUser().getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if current user owns the account by ID
     */
    public boolean isAccountOwner(Long accountId, Long userId) {
        try {
            Account account = accountRepository.findById(accountId)
                    .orElse(null);
            return account != null && account.getUser().getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Check if account exists
     */
    public boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    /**
     * Check if account is active
     */
    public boolean isAccountActive(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(Account::getIsActive)
                .orElse(false);
    }

    /**
     * Deactivate account
     */
    @Transactional
    public AccountDTO deactivateAccount(String accountNumber) {
        log.info("Deactivating account: {}", accountNumber);
        
        Account account = findAccountByNumber(accountNumber);
        account.deactivate();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Account deactivated: {}", accountNumber);
        
        auditService.logAccountDeactivation(savedAccount);
        return accountMapper.toDTO(savedAccount);
    }

    /**
     * Activate account
     */
    @Transactional
    public AccountDTO activateAccount(String accountNumber) {
        log.info("Activating account: {}", accountNumber);
        
        Account account = findAccountByNumber(accountNumber);
        account.activate();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Account activated: {}", accountNumber);
        
        auditService.logAccountActivation(savedAccount);
        return accountMapper.toDTO(savedAccount);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Current user not found: " + username));
    }

    /**
     * Find account by number or throw exception
     */
    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException("Account not found: " + accountNumber));
    }

    /**
     * Generate unique account number
     */
    private String generateAccountNumber() {
        String prefix = "ACC";
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000000);
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        String accountNumber = String.format("%s-%s-%s", prefix, timestamp, uuid);
        
        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            accountNumber = String.format("%s-%s-%s", prefix, timestamp, uuid);
        }
        
        return accountNumber;
    }

    /**
     * Validate account creation business rules
     */
    private void validateAccountCreation(CreateAccountRequest request, User user) {
        // Check if user already has this type of account
        List<Account> existingAccounts = accountRepository.findByUserIdAndAccountType(
                user.getId(), request.getAccountType());
        
        if (!existingAccounts.isEmpty()) {
            throw new BusinessException("User already has an account of type: " + 
                    request.getAccountType().getDisplayName());
        }
        
        // Validate initial deposit
        if (request.getInitialDeposit() != null && 
            request.getInitialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Initial deposit cannot be negative");
        }
        
        // Validate credit limit
        if (request.getCreditLimit() != null && 
            request.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Credit limit cannot be negative");
        }
    }

    /**
     * Validate deposit amount
     */
    private void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be positive");
        }
        
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new BusinessException("Deposit amount cannot exceed $1,000,000");
        }
    }

    /**
     * Validate withdrawal amount
     */
    private void validateWithdrawalAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Withdrawal amount must be positive");
        }
    }

    /**
     * Validate account for transaction
     */
    private void validateAccountForTransaction(Account account) {
        if (!account.getIsActive()) {
            throw new BusinessException("Account is not active");
        }
        
        if (account.getIsFrozen()) {
            throw new BusinessException("Account is frozen");
        }
    }
}
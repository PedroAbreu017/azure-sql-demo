// FinancialTransactionService.java - CORRIGIDO
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.dto.CreateTransactionRequest;
import com.example.azure_sql_demo.dto.FinancialTransactionDTO;
import com.example.azure_sql_demo.dto.TransferRequest;
import com.example.azure_sql_demo.exception.BusinessException;
import com.example.azure_sql_demo.mapper.FinancialTransactionMapper;
import com.example.azure_sql_demo.model.Account;
import com.example.azure_sql_demo.model.FinancialTransaction;
import com.example.azure_sql_demo.repository.AccountRepository;
import com.example.azure_sql_demo.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FinancialTransactionMapper transactionMapper;
    private final AuditService auditService;

    /**
     * Create and process a financial transaction
     */
    @Transactional
    public FinancialTransactionDTO createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction: {} for amount: {}", 
                request.getTransactionType(), request.getAmount());
        
        // Validate request
        validateTransactionRequest(request);
        
        // Create transaction entity
        FinancialTransaction transaction = transactionMapper.toEntity(request);
        
        // Set accounts
        setTransactionAccounts(transaction, request);
        
        // Generate reference number
        transaction.setReferenceNumber(generateReferenceNumber(request.getTransactionType()));
        
        // Save transaction
        FinancialTransaction savedTransaction = transactionRepository.save(transaction);
        
        // Process transaction based on type
        switch (request.getTransactionType()) { // ✅ Usa transactionType em vez de getType()
            case DEPOSIT -> processDeposit(savedTransaction);
            case WITHDRAWAL -> processWithdrawal(savedTransaction);
            case TRANSFER -> processTransfer(savedTransaction);
            case PAYMENT -> processPayment(savedTransaction);
            default -> throw new BusinessException("Unsupported transaction type: " + 
                    request.getTransactionType()); // ✅ Default case adicionado
        }
        
        // Audit log
        auditService.logFinancialTransaction(savedTransaction);
        
        log.info("Transaction created successfully with id: {}", savedTransaction.getId());
        return transactionMapper.toDTO(savedTransaction);
    }

    /**
     * Process transfer between accounts
     */
    @Transactional
    public FinancialTransactionDTO transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {} amount: {}", 
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        
        // Validate transfer request
        validateTransferRequest(request);
        
        // Get accounts
        Account fromAccount = findAccountByNumber(request.getFromAccountNumber());
        Account toAccount = findAccountByNumber(request.getToAccountNumber());
        
        // Validate transfer
        validateTransfer(fromAccount, toAccount, request.getAmount());
        
        // Create transaction
        FinancialTransaction transaction = FinancialTransaction.builder()
                .transactionType(FinancialTransaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .description(request.getDescription())
                .referenceNumber(request.getReferenceNumber() != null ? 
                        request.getReferenceNumber() : generateReferenceNumber(FinancialTransaction.TransactionType.TRANSFER))
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .status(FinancialTransaction.TransactionStatus.PENDING)
                .build();
        
        // Save transaction
        FinancialTransaction savedTransaction = transactionRepository.save(transaction);
        
        // Process transfer
        processTransfer(savedTransaction);
        
        // Audit log
        auditService.logFinancialTransaction(savedTransaction);
        
        log.info("Transfer completed successfully with id: {}", savedTransaction.getId());
        return transactionMapper.toDTO(savedTransaction);
    }

    /**
     * Get transaction by ID
     */
    public FinancialTransactionDTO getTransactionById(Long id) {
        log.info("Fetching transaction by id: {}", id);
        FinancialTransaction transaction = findTransactionById(id);
        return transactionMapper.toDTO(transaction);
    }

    /**
     * Get transactions by account number
     */
    public Page<FinancialTransactionDTO> getTransactionsByAccount(String accountNumber, Pageable pageable) {
        log.info("Fetching transactions for account: {}", accountNumber);
        
        Account account = findAccountByNumber(accountNumber);
        Page<FinancialTransaction> transactions = transactionRepository
                .findByFromAccountOrToAccountOrderByCreatedAtDesc(account, account, pageable);
        
        return transactions.map(transactionMapper::toDTO);
    }

    /**
     * Get user transactions
     */
    public Page<FinancialTransactionDTO> getUserTransactions(Long userId, Pageable pageable) {
        log.info("Fetching transactions for user: {}", userId);
        
        Page<FinancialTransaction> transactions = transactionRepository
                .findByFromAccount_UserIdOrToAccount_UserIdOrderByCreatedAtDesc(userId, userId, pageable);
        
        return transactions.map(transactionMapper::toDTO);
    }

    /**
     * Get transactions by status
     */
    public List<FinancialTransactionDTO> getTransactionsByStatus(FinancialTransaction.TransactionStatus status) {
        log.info("Fetching transactions by status: {}", status);
        
        List<FinancialTransaction> transactions = transactionRepository.findByStatusOrderByCreatedAtDesc(status);
        return transactionMapper.toDTOList(transactions);
    }

    /**
     * Cancel transaction
     */
    @Transactional
    public FinancialTransactionDTO cancelTransaction(Long transactionId) {
        log.info("Cancelling transaction id: {}", transactionId);
        
        FinancialTransaction transaction = findTransactionById(transactionId);
        
        if (transaction.getStatus() != FinancialTransaction.TransactionStatus.PENDING) {
            throw new BusinessException("Only pending transactions can be cancelled");
        }
        
        FinancialTransaction.TransactionStatus oldStatus = transaction.getStatus();
        transaction.cancel();
        
        FinancialTransaction savedTransaction = transactionRepository.save(transaction);
        
        // Audit log
        auditService.logTransactionStatusChange(savedTransaction, oldStatus);
        
        log.info("Transaction cancelled successfully: {}", transactionId);
        return transactionMapper.toDTO(savedTransaction);
    }

    // ========== TRANSACTION PROCESSING METHODS ==========

    /**
     * Process deposit transaction
     */
    private void processDeposit(FinancialTransaction transaction) {
        log.debug("Processing deposit transaction: {}", transaction.getId());
        
        try {
            Account account = transaction.getFromAccount();
            account.credit(transaction.getAmount());
            accountRepository.save(account);
            
            transaction.markAsCompleted();
            transactionRepository.save(transaction);
            
            log.debug("Deposit processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing deposit: {}", e.getMessage());
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new BusinessException("Failed to process deposit: " + e.getMessage());
        }
    }

    /**
     * Process withdrawal transaction
     */
    private void processWithdrawal(FinancialTransaction transaction) {
        log.debug("Processing withdrawal transaction: {}", transaction.getId());
        
        try {
            Account account = transaction.getFromAccount();
            
            if (!account.canDebit(transaction.getAmount())) {
                throw new BusinessException("Insufficient funds for withdrawal");
            }
            
            account.debit(transaction.getAmount());
            accountRepository.save(account);
            
            transaction.markAsCompleted();
            transactionRepository.save(transaction);
            
            log.debug("Withdrawal processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing withdrawal: {}", e.getMessage());
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new BusinessException("Failed to process withdrawal: " + e.getMessage());
        }
    }

    /**
     * Process transfer transaction
     */
    private void processTransfer(FinancialTransaction transaction) {
        log.debug("Processing transfer transaction: {}", transaction.getId());
        
        try {
            Account fromAccount = transaction.getFromAccount();
            Account toAccount = transaction.getToAccount();
            BigDecimal amount = transaction.getAmount();
            
            if (!fromAccount.canDebit(amount)) {
                throw new BusinessException("Insufficient funds for transfer");
            }
            
            // Debit from source account
            fromAccount.debit(amount);
            accountRepository.save(fromAccount);
            
            // Credit to destination account
            toAccount.credit(amount);
            accountRepository.save(toAccount);
            
            transaction.markAsCompleted();
            transactionRepository.save(transaction);
            
            // ✅ Método correto do AuditService
            auditService.logFinancialTransaction(transaction);
            
            log.debug("Transfer processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing transfer: {}", e.getMessage());
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new BusinessException("Failed to process transfer: " + e.getMessage());
        }
    }

    /**
     * Process payment transaction
     */
    private void processPayment(FinancialTransaction transaction) {
        log.debug("Processing payment transaction: {}", transaction.getId());
        
        try {
            Account account = transaction.getFromAccount();
            
            if (!account.canDebit(transaction.getAmount())) {
                throw new BusinessException("Insufficient funds for payment");
            }
            
            account.debit(transaction.getAmount());
            accountRepository.save(account);
            
            transaction.markAsCompleted();
            transactionRepository.save(transaction);
            
            log.debug("Payment processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new BusinessException("Failed to process payment: " + e.getMessage());
        }
    }

    /**
     * Transfer funds between accounts (método adicional)
     */
    @Transactional
    public FinancialTransactionDTO transferFunds(TransferRequest request, String initiatedBy) {
        log.info("Transfer funds initiated by {} from {} to {} amount: {}", 
                initiatedBy, request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        
        return transfer(request); // Usa o método transfer existente
    }

    /**
     * Get transaction by reference number
     */
    public FinancialTransactionDTO getTransactionByReference(String referenceNumber) {
        log.info("Fetching transaction by reference: {}", referenceNumber);
        
        FinancialTransaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BusinessException("Transaction not found with reference: " + referenceNumber));
        
        return transactionMapper.toDTO(transaction);
    }

    /**
     * Search transactions with criteria
     */
    public Page<FinancialTransactionDTO> searchTransactions(Long userId, LocalDateTime startDate, 
                                                           LocalDateTime endDate, String status, Pageable pageable) {
        log.info("Searching transactions for user: {} from {} to {} with status: {}", 
                userId, startDate, endDate, status);
        
        FinancialTransaction.TransactionStatus transactionStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                transactionStatus = FinancialTransaction.TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid transaction status: " + status);
            }
        }
        
        Page<FinancialTransaction> transactions = transactionRepository.findTransactionsByCriteria(
                userId, startDate, endDate, transactionStatus, null, null, null, pageable);
        
        return transactions.map(transactionMapper::toDTO);
    }

    /**
     * Search transactions (overloaded method for compatibility)
     */
    public Page<FinancialTransactionDTO> searchTransactions(Long userId, LocalDateTime startDate, 
                                                           LocalDateTime endDate, String status) {
        return searchTransactions(userId, startDate, endDate, status, 
                org.springframework.data.domain.PageRequest.of(0, 20));
    }

    /**
     * Set transaction accounts based on request
     */
    private void setTransactionAccounts(FinancialTransaction transaction, CreateTransactionRequest request) {
        // Set from account
        Account fromAccount = findAccountByNumber(request.getFromAccountNumber());
        transaction.setFromAccount(fromAccount);
        
        // Set to account if provided
        if (request.getToAccountNumber() != null) {
            Account toAccount = findAccountByNumber(request.getToAccountNumber());
            transaction.setToAccount(toAccount);
        }
    }

    /**
     * Generate reference number for transaction
     */
    private String generateReferenceNumber(FinancialTransaction.TransactionType type) { // ✅ Parâmetro correto
        String prefix = switch (type) { // ✅ Switch com default
            case DEPOSIT -> "DEP";
            case WITHDRAWAL -> "WDR";
            case TRANSFER -> "TRF";
            case PAYMENT -> "PAY";
            case REFUND -> "REF";
            case FEE -> "FEE";
            case INTEREST -> "INT";
            default -> "TXN"; // ✅ Default case
        };
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return String.format("%s-%s-%s", prefix, timestamp, uuid);
    }

    /**
     * Find account by number or throw exception
     */
    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException("Account not found: " + accountNumber));
    }

    /**
     * Find transaction by ID or throw exception
     */
    private FinancialTransaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Transaction not found with id: " + id));
    }

    /**
     * Validate transaction request
     */
    private void validateTransactionRequest(CreateTransactionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transaction amount must be positive");
        }
        
        if (request.getFromAccountNumber() == null || request.getFromAccountNumber().trim().isEmpty()) {
            throw new BusinessException("From account number is required");
        }
        
        // Validate to account for transfer transactions
        if (request.getTransactionType() == FinancialTransaction.TransactionType.TRANSFER) {
            if (request.getToAccountNumber() == null || request.getToAccountNumber().trim().isEmpty()) {
                throw new BusinessException("To account number is required for transfers");
            }
            
            if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
                throw new BusinessException("Cannot transfer to the same account");
            }
        }
    }

    /**
     * Validate transfer request
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be positive");
        }
        
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new BusinessException("Cannot transfer to the same account");
        }
    }

    /**
     * Validate transfer between accounts
     */
    private void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (!fromAccount.getIsActive()) {
            throw new BusinessException("Source account is not active");
        }
        
        if (!toAccount.getIsActive()) {
            throw new BusinessException("Destination account is not active");
        }
        
        if (fromAccount.getIsFrozen()) {
            throw new BusinessException("Source account is frozen");
        }
        
        if (toAccount.getIsFrozen()) {
            throw new BusinessException("Destination account is frozen");
        }
        
        if (!fromAccount.canDebit(amount)) {
            throw new BusinessException("Insufficient funds in source account");
        }
    }
}
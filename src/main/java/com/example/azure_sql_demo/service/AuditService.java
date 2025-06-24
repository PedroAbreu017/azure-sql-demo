// AuditService.java - COMPLETO
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.model.*;
import com.example.azure_sql_demo.repository.FinancialAuditLogRepository;
import com.example.azure_sql_demo.repository.ProductAuditLogRepository;
import com.example.azure_sql_demo.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

    private final ProductAuditLogRepository productAuditLogRepository;
    private final FinancialAuditLogRepository financialAuditLogRepository;
    private final ObjectMapper objectMapper;

    // ========== PRODUCT AUDIT METHODS ==========

    /**
     * Log product creation
     */
    public void logProductCreation(Product product) {
        log.info("Logging product creation for product id: {}", product.getId());
        
        try {
            String newValues = objectMapper.writeValueAsString(createProductAuditData(product));
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(product.getId())
                    .action("CREATE")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Product creation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging product creation: ", e);
        }
    }

    /**
     * Log product update
     */
    public void logProductUpdate(Product originalProduct, Product updatedProduct) {
        log.info("Logging product update for product id: {}", updatedProduct.getId());
        
        try {
            String oldValues = objectMapper.writeValueAsString(createProductAuditData(originalProduct));
            String newValues = objectMapper.writeValueAsString(createProductAuditData(updatedProduct));
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(updatedProduct.getId())
                    .action("UPDATE")
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Product update logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging product update: ", e);
        }
    }

    /**
     * Log product deletion (soft delete)
     */
    public void logProductDeletion(Product product) {
        log.info("Logging product deletion for product id: {}", product.getId());
        
        try {
            String oldValues = objectMapper.writeValueAsString(createProductAuditData(product));
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(product.getId())
                    .action("DELETE")
                    .oldValues(oldValues)
                    .newValues(null)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Product deletion logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging product deletion: ", e);
        }
    }

    /**
     * Log product activation
     */
    public void logProductActivation(Product product) {
        log.info("Logging product activation for product id: {}", product.getId());
        
        try {
            String newValues = objectMapper.writeValueAsString(createProductAuditData(product));
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(product.getId())
                    .action("ACTIVATE")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Product activation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging product activation: ", e);
        }
    }

    /**
     * Log product deactivation
     */
    public void logProductDeactivation(Product product) {
        log.info("Logging product deactivation for product id: {}", product.getId());
        
        try {
            String newValues = objectMapper.writeValueAsString(createProductAuditData(product));
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(product.getId())
                    .action("DEACTIVATE")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Product deactivation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging product deactivation: ", e);
        }
    }

    /**
     * Log stock update
     */
    public void logStockUpdate(Product product, Integer oldQuantity, Integer newQuantity) {
        log.info("Logging stock update for product id: {} from {} to {}", 
                product.getId(), oldQuantity, newQuantity);
        
        try {
            StockUpdateData stockData = StockUpdateData.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .oldQuantity(oldQuantity)
                    .newQuantity(newQuantity)
                    .difference(newQuantity - oldQuantity)
                    .build();
            
            String newValues = objectMapper.writeValueAsString(stockData);
            
            ProductAuditLog auditLog = ProductAuditLog.builder()
                    .productId(product.getId())
                    .action("STOCK_UPDATE")
                    .oldValues(String.valueOf(oldQuantity))
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            productAuditLogRepository.save(auditLog);
            log.debug("Stock update logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging stock update: ", e);
        }
    }

    // ========== ACCOUNT AUDIT METHODS ==========

    /**
     * Log account creation
     */
    public void logAccountCreation(Account account) {
        log.info("Logging account creation for account: {}", account.getAccountNumber());
        
        try {
            String newValues = objectMapper.writeValueAsString(createAccountAuditData(account));
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .action("CREATE_ACCOUNT")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Account creation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging account creation: ", e);
        }
    }

    /**
     * Log account deposit
     */
    public void logAccountDeposit(Account account, BigDecimal amount, BigDecimal oldBalance) {
        log.info("Logging deposit of {} to account: {}", amount, account.getAccountNumber());
        
        try {
            DepositWithdrawalData transactionData = DepositWithdrawalData.builder()
                    .accountNumber(account.getAccountNumber())
                    .transactionType("DEPOSIT")
                    .amount(amount)
                    .oldBalance(oldBalance)
                    .newBalance(account.getBalance())
                    .transactionDate(LocalDateTime.now())
                    .build();
            
            String newValues = objectMapper.writeValueAsString(transactionData);
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .action("DEPOSIT")
                    .oldValues(oldBalance.toString())
                    .newValues(newValues)
                    .amount(amount)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Account deposit logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging account deposit: ", e);
        }
    }

    /**
     * Log account withdrawal
     */
    public void logAccountWithdrawal(Account account, BigDecimal amount, BigDecimal oldBalance) {
        log.info("Logging withdrawal of {} from account: {}", amount, account.getAccountNumber());
        
        try {
            DepositWithdrawalData transactionData = DepositWithdrawalData.builder()
                    .accountNumber(account.getAccountNumber())
                    .transactionType("WITHDRAWAL")
                    .amount(amount)
                    .oldBalance(oldBalance)
                    .newBalance(account.getBalance())
                    .transactionDate(LocalDateTime.now())
                    .build();
            
            String newValues = objectMapper.writeValueAsString(transactionData);
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .action("WITHDRAWAL")
                    .oldValues(oldBalance.toString())
                    .newValues(newValues)
                    .amount(amount)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Account withdrawal logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging account withdrawal: ", e);
        }
    }

    /**
     * Log account activation
     */
    public void logAccountActivation(Account account) {
        log.info("Logging account activation for account: {}", account.getAccountNumber());
        
        try {
            String newValues = objectMapper.writeValueAsString(createAccountAuditData(account));
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .action("ACTIVATE_ACCOUNT")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Account activation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging account activation: ", e);
        }
    }

    /**
     * Log account deactivation
     */
    public void logAccountDeactivation(Account account) {
        log.info("Logging account deactivation for account: {}", account.getAccountNumber());
        
        try {
            String newValues = objectMapper.writeValueAsString(createAccountAuditData(account));
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .accountId(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .action("DEACTIVATE_ACCOUNT")
                    .oldValues(null)
                    .newValues(newValues)
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Account deactivation logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging account deactivation: ", e);
        }
    }

    // ========== TRANSACTION AUDIT METHODS ==========

    /**
     * Log financial transaction
     */
    public void logFinancialTransaction(FinancialTransaction transaction) {
        log.info("Logging financial transaction id: {}", transaction.getId());
        
        try {
            String newValues = objectMapper.writeValueAsString(createTransactionAuditData(transaction));
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .transactionId(transaction.getId())
                    .accountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null)
                    .accountNumber(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null)
                    .action("TRANSACTION_" + transaction.getTransactionType().name())
                    .oldValues(null)
                    .newValues(newValues)
                    .amount(transaction.getAmount())
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Financial transaction logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging financial transaction: ", e);
        }
    }

    /**
     * Log transaction status change
     */
    public void logTransactionStatusChange(FinancialTransaction transaction, 
                                         FinancialTransaction.TransactionStatus oldStatus) {
        log.info("Logging transaction status change for transaction id: {} from {} to {}", 
                transaction.getId(), oldStatus, transaction.getStatus());
        
        try {
            TransactionStatusChangeData statusData = TransactionStatusChangeData.builder()
                    .transactionId(transaction.getId())
                    .oldStatus(oldStatus.name())
                    .newStatus(transaction.getStatus().name())
                    .errorMessage(transaction.getErrorMessage())
                    .processedAt(transaction.getProcessedAt())
                    .build();
            
            String newValues = objectMapper.writeValueAsString(statusData);
            
            FinancialAuditLog auditLog = FinancialAuditLog.builder()
                    .transactionId(transaction.getId())
                    .accountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null)
                    .accountNumber(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null)
                    .action("STATUS_CHANGE")
                    .oldValues(oldStatus.name())
                    .newValues(newValues)
                    .amount(transaction.getAmount())
                    .userId(getCurrentUserId())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            financialAuditLogRepository.save(auditLog);
            log.debug("Transaction status change logged successfully");
            
        } catch (JsonProcessingException e) {
            log.error("Error logging transaction status change: ", e);
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Get current authenticated user ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                return ((UserDetailsImpl) authentication.getPrincipal()).getId();
            }
            return null;
        } catch (Exception e) {
            log.warn("Could not get current user ID: ", e);
            return null;
        }
    }

    /**
     * Create product audit data
     */
    private ProductAuditData createProductAuditData(Product product) {
        return ProductAuditData.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .isActive(product.getIsActive())
                .build();
    }

    /**
     * Create account audit data
     */
    private AccountAuditData createAccountAuditData(Account account) {
        return AccountAuditData.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .creditLimit(account.getCreditLimit())
                .isActive(account.getIsActive())
                .isFrozen(account.getIsFrozen())
                .currency(account.getCurrency())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .build();
    }

    /**
     * Create transaction audit data
     */
    private TransactionAuditData createTransactionAuditData(FinancialTransaction transaction) {
        return TransactionAuditData.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType().name())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .referenceNumber(transaction.getReferenceNumber())
                .status(transaction.getStatus().name())
                .fromAccountNumber(transaction.getFromAccount() != null ? 
                        transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ? 
                        transaction.getToAccount().getAccountNumber() : null)
                .processedAt(transaction.getProcessedAt())
                .build();
    }

    // ========== AUDIT DATA CLASSES ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ProductAuditData {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer quantity;
        private String category;
        private Boolean isActive;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class AccountAuditData {
        private Long id;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private BigDecimal creditLimit;
        private Boolean isActive;
        private Boolean isFrozen;
        private String currency;
        private Long userId;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class TransactionAuditData {
        private Long id;
        private String transactionType;
        private BigDecimal amount;
        private String description;
        private String referenceNumber;
        private String status;
        private String fromAccountNumber;
        private String toAccountNumber;
        private LocalDateTime processedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DepositWithdrawalData {
        private String accountNumber;
        private String transactionType;
        private BigDecimal amount;
        private BigDecimal oldBalance;
        private BigDecimal newBalance;
        private LocalDateTime transactionDate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class StockUpdateData {
        private Long productId;
        private String productName;
        private Integer oldQuantity;
        private Integer newQuantity;
        private Integer difference;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class TransactionStatusChangeData {
        private Long transactionId;
        private String oldStatus;
        private String newStatus;
        private String errorMessage;
        private LocalDateTime processedAt;
    }
}
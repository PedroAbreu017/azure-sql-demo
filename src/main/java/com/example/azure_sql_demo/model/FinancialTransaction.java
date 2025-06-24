// FinancialTransaction.java
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"fromAccount", "toAccount"})
@ToString(exclude = {"fromAccount", "toAccount"})
public class FinancialTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;
    
    @Column(length = 255)
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Column(name = "reference_number", length = 50)
    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "error_message", length = 500)
    @Size(max = 500, message = "Error message cannot exceed 500 characters")
    private String errorMessage;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;
    
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;
    
    // Enums
    public enum TransactionType {
        DEPOSIT("Deposit"),
        WITHDRAWAL("Withdrawal"),
        TRANSFER("Transfer"),
        PAYMENT("Payment"),
        REFUND("Refund"),
        FEE("Fee"),
        INTEREST("Interest");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum TransactionStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled"),
        REVERSED("Reversed");
        
        private final String displayName;
        
        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean isTerminal() {
            return this == COMPLETED || this == FAILED || this == CANCELLED || this == REVERSED;
        }
        
        public boolean isSuccessful() {
            return this == COMPLETED;
        }
    }
    
    // Business methods
    public boolean isDebit() {
        return transactionType == TransactionType.WITHDRAWAL || 
               transactionType == TransactionType.TRANSFER && fromAccount != null ||
               transactionType == TransactionType.PAYMENT ||
               transactionType == TransactionType.FEE;
    }
    
    public boolean isCredit() {
        return transactionType == TransactionType.DEPOSIT ||
               transactionType == TransactionType.TRANSFER && toAccount != null ||
               transactionType == TransactionType.REFUND ||
               transactionType == TransactionType.INTEREST;
    }
    
    public boolean canBeReversed() {
        return status == TransactionStatus.COMPLETED && 
               (transactionType == TransactionType.TRANSFER || 
                transactionType == TransactionType.PAYMENT);
    }
    
    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsProcessing() {
        this.status = TransactionStatus.PROCESSING;
    }
    
    public void cancel() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transactions can be cancelled");
        }
        this.status = TransactionStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void reverse() {
        if (!canBeReversed()) {
            throw new IllegalStateException("Transaction cannot be reversed");
        }
        this.status = TransactionStatus.REVERSED;
    }
    
    public BigDecimal getNetAmount() {
        return amount;
    }
    
    public String getFormattedAmount() {
        return String.format("$%.2f", amount);
    }
}
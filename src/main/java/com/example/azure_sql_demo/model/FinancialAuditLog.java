// FinancialAuditLog.java
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_audit_log", indexes = {
    @Index(name = "idx_financial_audit_account_id", columnList = "account_id"),
    @Index(name = "idx_financial_audit_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_financial_audit_action", columnList = "action"),
    @Index(name = "idx_financial_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_financial_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_financial_audit_account_number", columnList = "account_number"),
    @Index(name = "idx_financial_audit_amount", columnList = "amount")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"account", "transaction", "user"})
@ToString(exclude = {"account", "transaction", "user"})
public class FinancialAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", length = 50)
    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action cannot exceed 50 characters")
    private String action;

    @Column(name = "old_values", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    private String userAgent;

    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "correlation_id", length = 100)
    @Size(max = 100, message = "Correlation ID cannot exceed 100 characters")
    private String correlationId;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships (optional - for easier querying)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private FinancialTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Business methods
    public boolean isAccountAction() {
        return action != null && (action.contains("ACCOUNT") || 
                                action.equals("DEPOSIT") || 
                                action.equals("WITHDRAWAL"));
    }

    public boolean isTransactionAction() {
        return action != null && action.startsWith("TRANSACTION_");
    }

    public boolean isHighValueTransaction() {
        return amount != null && amount.compareTo(new BigDecimal("10000")) >= 0;
    }

    public boolean isDebitAction() {
        return action != null && (action.equals("WITHDRAWAL") || 
                                action.contains("TRANSFER") || 
                                action.contains("PAYMENT"));
    }

    public boolean isCreditAction() {
        return action != null && (action.equals("DEPOSIT") || 
                                action.contains("REFUND") || 
                                action.contains("INTEREST"));
    }

    public boolean hasAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getFormattedAmount() {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }

    // Enum for common financial actions
    public enum FinancialAuditAction {
        CREATE_ACCOUNT("CREATE_ACCOUNT"),
        ACTIVATE_ACCOUNT("ACTIVATE_ACCOUNT"),
        DEACTIVATE_ACCOUNT("DEACTIVATE_ACCOUNT"),
        FREEZE_ACCOUNT("FREEZE_ACCOUNT"),
        UNFREEZE_ACCOUNT("UNFREEZE_ACCOUNT"),
        DEPOSIT("DEPOSIT"),
        WITHDRAWAL("WITHDRAWAL"),
        TRANSFER("TRANSFER"),
        TRANSACTION_CREATE("TRANSACTION_CREATE"),
        TRANSACTION_APPROVE("TRANSACTION_APPROVE"),
        TRANSACTION_REJECT("TRANSACTION_REJECT"),
        TRANSACTION_CANCEL("TRANSACTION_CANCEL"),
        STATUS_CHANGE("STATUS_CHANGE"),
        BALANCE_ADJUSTMENT("BALANCE_ADJUSTMENT"),
        CREDIT_LIMIT_UPDATE("CREDIT_LIMIT_UPDATE");

        private final String value;

        FinancialAuditAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
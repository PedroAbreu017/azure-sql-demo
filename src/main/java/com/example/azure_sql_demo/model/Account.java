// Account.java
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "account_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user", "fromTransactions", "toTransactions"})
@ToString(exclude = {"user", "fromTransactions", "toTransactions"})
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Balance format is invalid")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "credit_limit", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Credit limit cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_frozen", nullable = false)
    @Builder.Default
    private Boolean isFrozen = false;
    
    @Column(name = "currency", length = 3)
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    @Builder.Default
    private String currency = "USD";
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Account must belong to a user")
    private User user;
    
    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FinancialTransaction> fromTransactions = new ArrayList<>();
    
    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FinancialTransaction> toTransactions = new ArrayList<>();
    
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
    
    // Enum for Account Types
    public enum AccountType {
        CHECKING("Checking Account"),
        SAVINGS("Savings Account"),
        CREDIT("Credit Account"),
        INVESTMENT("Investment Account"),
        BUSINESS("Business Account");
        
        private final String displayName;
        
        AccountType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Business methods
    public boolean canDebit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (!isActive || isFrozen) {
            return false;
        }
        
        BigDecimal availableBalance = getAvailableBalance();
        return availableBalance.compareTo(amount) >= 0;
    }
    
    public BigDecimal getAvailableBalance() {
        BigDecimal available = balance;
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            available = available.add(creditLimit);
        }
        return available;
    }
    
    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalArgumentException("Insufficient funds or account is inactive/frozen");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        if (!isActive) {
            throw new IllegalStateException("Cannot credit to inactive account");
        }
        
        this.balance = this.balance.add(amount);
    }
    
    public boolean isOverdrawn() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public boolean isWithinCreditLimit() {
        if (creditLimit == null) {
            return balance.compareTo(BigDecimal.ZERO) >= 0;
        }
        return balance.add(creditLimit).compareTo(BigDecimal.ZERO) >= 0;
    }
    
    public void freeze() {
        this.isFrozen = true;
    }
    
    public void unfreeze() {
        this.isFrozen = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
}


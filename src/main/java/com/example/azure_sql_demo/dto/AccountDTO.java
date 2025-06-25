// AccountDTO.java
package com.example.azure_sql_demo.dto;

import com.example.azure_sql_demo.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    
    private Long id;
    private String accountNumber;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private Boolean isActive;
    private Boolean isFrozen;
    private String currency;
    private String ownerName;
    private Long userId;
    private LocalDateTime createdAt;
    
    public BigDecimal getAvailableBalance() {
        BigDecimal available = balance != null ? balance : BigDecimal.ZERO;
        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            available = available.add(creditLimit);
        }
        return available;
    }
    
    public String getFormattedBalance() {
        return balance != null ? String.format("$%.2f", balance) : "$0.00";
    }
    
    public String getAccountTypeName() {
        return accountType != null ? accountType.getDisplayName() : "Unknown";
    }
}

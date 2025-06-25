// FinancialTransactionDTO.java
package com.example.azure_sql_demo.dto;

import com.example.azure_sql_demo.model.FinancialTransaction;
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
public class FinancialTransactionDTO {
    
    private Long id;
    private FinancialTransaction.TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private String referenceNumber;
    private FinancialTransaction.TransactionStatus status;
    private String errorMessage;
    private LocalDateTime processedAt;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;
    
    public String getFormattedAmount() {
        return amount != null ? String.format("$%.2f", amount) : "$0.00";
    }
    
    public String getTransactionTypeName() {
        return transactionType != null ? transactionType.getDisplayName() : "Unknown";
    }
    
    public String getStatusName() {
        return status != null ? status.getDisplayName() : "Unknown";
    }
    
    public boolean isCompleted() {
        return status == FinancialTransaction.TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == FinancialTransaction.TransactionStatus.FAILED;
    }
}

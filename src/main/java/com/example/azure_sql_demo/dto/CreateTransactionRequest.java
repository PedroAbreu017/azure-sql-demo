// CreateTransactionRequest.java
package com.example.azure_sql_demo.dto;

import com.example.azure_sql_demo.model.FinancialTransaction;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    
    @NotBlank(message = "From account number is required")
    private String fromAccountNumber;
    
    private String toAccountNumber; // Optional for deposits/withdrawals
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    private FinancialTransaction.TransactionType transactionType;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;
}

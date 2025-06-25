// CreateAccountRequest.java
package com.example.azure_sql_demo.dto;

import com.example.azure_sql_demo.model.Account;
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
public class CreateAccountRequest {
    
    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
    
    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Initial deposit format is invalid")
    private BigDecimal initialDeposit;
    
    @DecimalMin(value = "0.00", message = "Credit limit cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
    
    @Size(max = 3, message = "Currency code cannot exceed 3 characters")
    @Builder.Default
    private String currency = "USD";
}

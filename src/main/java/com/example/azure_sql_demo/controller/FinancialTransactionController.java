
// FinancialTransactionController.java - CORRIGIDO
package com.example.azure_sql_demo.controller;

import com.example.azure_sql_demo.dto.CreateTransactionRequest;
import com.example.azure_sql_demo.dto.FinancialTransactionDTO;
import com.example.azure_sql_demo.dto.TransferRequest;
import com.example.azure_sql_demo.model.FinancialTransaction;
import com.example.azure_sql_demo.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Financial Transaction Management", description = "APIs for managing financial transactions")
@SecurityRequirement(name = "bearerAuth")
public class FinancialTransactionController {

    private final FinancialTransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates a new financial transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<FinancialTransactionDTO> createTransaction(
            @Parameter(description = "Transaction creation request")
            @Valid @RequestBody CreateTransactionRequest request) {
        
        log.info("Creating transaction: {}", request.getTransactionType());
        FinancialTransactionDTO transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfer funds between accounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transfer data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<FinancialTransactionDTO> transfer(
            @Parameter(description = "Transfer request")
            @Valid @RequestBody TransferRequest request) {
        
        log.info("Processing transfer from {} to {}", 
                request.getFromAccountNumber(), request.getToAccountNumber());
        FinancialTransactionDTO transaction = transactionService.transfer(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transfer-funds")
    @Operation(summary = "Transfer funds with initiator", description = "Transfer funds between accounts with initiator tracking")
    public ResponseEntity<FinancialTransactionDTO> transferFunds(
            @Parameter(description = "Transfer request")
            @Valid @RequestBody TransferRequest request,
            @Parameter(description = "User who initiated the transfer")
            @RequestParam(required = false) String initiatedBy,
            Authentication authentication) {
        
        String initiator = initiatedBy != null ? initiatedBy : authentication.getName();
        FinancialTransactionDTO transaction = transactionService.transferFunds(request, initiator);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FinancialTransactionDTO> getTransactionById(
            @Parameter(description = "Transaction ID")
            @PathVariable Long id) {
        
        log.info("Fetching transaction by id: {}", id);
        FinancialTransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get transaction by reference", description = "Retrieves a transaction by its reference number")
    public ResponseEntity<FinancialTransactionDTO> getTransactionByReference(
            @Parameter(description = "Transaction reference number")
            @PathVariable String referenceNumber) {
        
        log.info("Fetching transaction by reference: {}", referenceNumber);
        FinancialTransactionDTO transaction = transactionService.getTransactionByReference(referenceNumber);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("@accountService.isAccountOwner(#accountNumber, authentication.principal.id) or hasRole('ADMIN')")
    @Operation(summary = "Get account transactions", description = "Retrieves transactions for a specific account")
    public ResponseEntity<Page<FinancialTransactionDTO>> getAccountTransactions(
            @Parameter(description = "Account number")
            @PathVariable String accountNumber,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching transactions for account: {}", accountNumber);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        Page<FinancialTransactionDTO> transactions = 
            transactionService.getTransactionsByAccount(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "Get user transactions", description = "Retrieves transactions for a specific user")
    public ResponseEntity<Page<FinancialTransactionDTO>> getUserTransactions(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Fetching transactions for user: {}", userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FinancialTransactionDTO> transactions = 
            transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get transactions by status", description = "Retrieves transactions by status")
    public ResponseEntity<List<FinancialTransactionDTO>> getTransactionsByStatus(
            @Parameter(description = "Transaction status")
            @PathVariable String status) {
        
        log.info("Fetching transactions by status: {}", status);
        
        try {
            FinancialTransaction.TransactionStatus transactionStatus = 
                FinancialTransaction.TransactionStatus.valueOf(status.toUpperCase());
            
            List<FinancialTransactionDTO> transactions = 
                transactionService.getTransactionsByStatus(transactionStatus);
            return ResponseEntity.ok(transactions);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transaction status: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search transactions", description = "Search transactions with multiple criteria")
    public ResponseEntity<Page<FinancialTransactionDTO>> searchTransactions(
            @Parameter(description = "User ID (optional)")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Start date (yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Transaction status")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Searching transactions with criteria - userId: {}, startDate: {}, endDate: {}, status: {}", 
                userId, startDate, endDate, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FinancialTransactionDTO> transactions = 
            transactionService.searchTransactions(userId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Cancel transaction", description = "Cancels a pending transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction cancelled successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<FinancialTransactionDTO> cancelTransaction(
            @Parameter(description = "Transaction ID")
            @PathVariable Long id) {
        
        log.info("Cancelling transaction id: {}", id);
        FinancialTransactionDTO transaction = transactionService.cancelTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions", description = "Retrieves recent transactions")
    public ResponseEntity<List<FinancialTransactionDTO>> getRecentTransactions(
            @Parameter(description = "Number of hours to look back")
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("Fetching transactions from last {} hours", hours);
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        Page<FinancialTransactionDTO> transactions = transactionService.searchTransactions(
                null, since, null, null, PageRequest.of(0, 20));
        return ResponseEntity.ok(transactions.getContent());
    }
}

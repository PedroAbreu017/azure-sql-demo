// AccountController.java
package com.example.azure_sql_demo.controller;

import com.example.azure_sql_demo.dto.AccountDTO;
import com.example.azure_sql_demo.dto.CreateAccountRequest;
import com.example.azure_sql_demo.dto.TransactionRequest;
import com.example.azure_sql_demo.security.UserDetailsImpl;
import com.example.azure_sql_demo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management API", description = "Operations related to financial accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create account", description = "Creates a new financial account")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/my-accounts")
    @Operation(summary = "Get user accounts", description = "Retrieves all accounts for the current user")
    public ResponseEntity<List<AccountDTO>> getMyAccounts(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.getUserAccounts(userDetails.getId()));
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("@accountService.isAccountOwner(#accountNumber, authentication.principal.id) or hasRole('ADMIN')")
    @Operation(summary = "Get account details", description = "Retrieves account details by account number")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @PostMapping("/{accountNumber}/deposit")
    @PreAuthorize("@accountService.isAccountOwner(#accountNumber, authentication.principal.id) or hasRole('ADMIN')")
    @Operation(summary = "Deposit funds", description = "Deposits funds into an account")
    public ResponseEntity<AccountDTO> deposit(@PathVariable String accountNumber, 
                                            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request.getAmount()));
    }

    @PostMapping("/{accountNumber}/withdraw")
    @PreAuthorize("@accountService.isAccountOwner(#accountNumber, authentication.principal.id) or hasRole('ADMIN')")
    @Operation(summary = "Withdraw funds", description = "Withdraws funds from an account")
    public ResponseEntity<AccountDTO> withdraw(@PathVariable String accountNumber, 
                                             @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request.getAmount()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Get accounts by user", description = "Retrieves all accounts for a specific user")
    public ResponseEntity<List<AccountDTO>> getAccountsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }
}

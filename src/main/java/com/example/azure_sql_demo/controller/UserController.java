// UserController.java - CORRIGIDO
package com.example.azure_sql_demo.controller;

import com.example.azure_sql_demo.dto.ChangePasswordRequest;
import com.example.azure_sql_demo.dto.UserDTO;
import com.example.azure_sql_demo.dto.UserRegistrationRequest;
import com.example.azure_sql_demo.service.UserService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Fetching all users - page: {}, size: {}", page, size);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get all active users", description = "Retrieves all active users")
    public ResponseEntity<List<UserDTO>> getAllActiveUsers() {
        log.info("Fetching all active users");
        List<UserDTO> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        log.info("Fetching user by id: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user", description = "Retrieves the current authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.info("Fetching current user");
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User registration request")
            @Valid @RequestBody UserRegistrationRequest request) {
        
        log.info("Creating new user: {}", request.getUsername());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "User data")
            @Valid @RequestBody UserDTO userDTO) {
        
        log.info("Updating user with id: {}", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "Update user profile", description = "Updates user profile using registration request")
    public ResponseEntity<UserDTO> updateUserProfile(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "User registration request")
            @Valid @RequestBody UserRegistrationRequest request) {
        
        log.info("Updating user profile with id: {}", id);
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable user", description = "Enables a disabled user")
    public ResponseEntity<UserDTO> enableUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        log.info("Enabling user with id: {}", id);
        UserDTO user = userService.enableUser(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable user", description = "Disables an active user")
    public ResponseEntity<UserDTO> disableUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        log.info("Disabling user with id: {}", id);
        UserDTO user = userService.disableUser(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account")
    public ResponseEntity<UserDTO> deactivateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        log.info("Deactivating user with id: {}", id);
        UserDTO user = userService.deactivateUser(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @Operation(summary = "Change password", description = "Changes user password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "Password change request")
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("Changing password for user id: {}", id);
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password/simple")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change password (admin)", description = "Admin changes user password with simple parameters")
    public ResponseEntity<Void> changePasswordSimple(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "Current password")
            @RequestParam String currentPassword,
            @Parameter(description = "New password")
            @RequestParam String newPassword) {
        
        log.info("Admin changing password for user id: {}", id);
        userService.changePassword(id, currentPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add role to user", description = "Adds a role to a user")
    public ResponseEntity<UserDTO> addRole(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "Role name")
            @PathVariable String roleName) {
        
        log.info("Adding role {} to user id: {}", roleName, id);
        UserDTO user = userService.addRoleToUser(id, roleName);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user")
    public ResponseEntity<UserDTO> removeRole(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Parameter(description = "Role name")
            @PathVariable String roleName) {
        
        log.info("Removing role {} from user id: {}", roleName, id);
        UserDTO user = userService.removeRoleFromUser(id, roleName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Retrieves users with a specific role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(
            @Parameter(description = "Role name")
            @PathVariable String roleName) {
        
        log.info("Fetching users by role: {}", roleName);
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Search users", description = "Search users by term")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @Parameter(description = "Search term")
            @RequestParam String searchTerm,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Searching users with term: {}", searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recently active users", description = "Retrieves users active since specified date")
    public ResponseEntity<List<UserDTO>> getRecentlyActiveUsers(
            @Parameter(description = "Since date (yyyy-MM-ddTHH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        log.info("Fetching users active since: {}", since);
        List<UserDTO> users = userService.getRecentlyActiveUsers(since);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/failed-logins")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users with failed logins", description = "Retrieves users with failed login attempts above threshold")
    public ResponseEntity<List<UserDTO>> getUsersWithFailedLogins(
            @Parameter(description = "Failed login threshold")
            @RequestParam(defaultValue = "3") int threshold) {
        
        log.info("Fetching users with failed logins > {}", threshold);
        List<UserDTO> users = userService.getUsersWithFailedLogins(threshold);
        return ResponseEntity.ok(users);
    }
}
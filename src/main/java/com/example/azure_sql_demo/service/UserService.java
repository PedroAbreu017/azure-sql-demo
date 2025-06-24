// UserService.java - CORRIGIDO
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.dto.ChangePasswordRequest;
import com.example.azure_sql_demo.dto.UserDTO;
import com.example.azure_sql_demo.dto.UserRegistrationRequest;
import com.example.azure_sql_demo.exception.BusinessException;
import com.example.azure_sql_demo.mapper.UserMapper;
import com.example.azure_sql_demo.model.Role;
import com.example.azure_sql_demo.model.User;
import com.example.azure_sql_demo.repository.RoleRepository;
import com.example.azure_sql_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Get all users with pagination
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDTO);
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = findUserById(id);
        return userMapper.toDTO(user);
    }

    /**
     * Get user by username
     */
    public UserDTO getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found: " + username));
        return userMapper.toDTO(user);
    }

    /**
     * Get current authenticated user
     */
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Fetching current user: {}", username);
        return getUserByUsername(username);
    }

    /**
     * Create new user (admin function)
     */
    @Transactional
    public UserDTO createUser(UserRegistrationRequest request) {
        log.info("Creating new user: {}", request.getUsername());
        
        // Validate passwords match
        if (!request.isPasswordMatching()) {
            throw new BusinessException("Passwords do not match");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }
        
        // Create user entity
        User user = userMapper.toEntity(request);
        
        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set default role
        Role defaultRole = roleRepository.findByName("USER") // ✅ String correto
                .orElseThrow(() -> new BusinessException("Default role not found"));
        
        user.setRoles(new HashSet<>(Set.of(defaultRole)));
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUsername());
        
        return userMapper.toDTO(savedUser);
    }

    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(Long id, UserRegistrationRequest request) {
        log.info("Updating user with id: {}", id);
        
        User existingUser = findUserById(id);
        
        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(request.getUsername()) &&
            userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }
        
        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessException("Email already exists: " + request.getEmail());
        }
        
        // Update fields
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (!request.isPasswordMatching()) {
                throw new BusinessException("Passwords do not match");
            }
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Enable user
     */
    @Transactional
    public UserDTO enableUser(Long id) {
        log.info("Enabling user with id: {}", id);
        
        User user = findUserById(id);
        user.setIsEnabled(true); // ✅ setIsEnabled correto
        
        User savedUser = userRepository.save(user);
        log.info("User enabled successfully: {}", savedUser.getUsername());
        
        return userMapper.toDTO(savedUser);
    }

    /**
     * Disable user
     */
    @Transactional
    public UserDTO disableUser(Long id) {
        log.info("Disabling user with id: {}", id);
        
        User user = findUserById(id);
        user.setIsEnabled(false); // ✅ setIsEnabled correto
        
        User savedUser = userRepository.save(user);
        log.info("User disabled successfully: {}", savedUser.getUsername());
        
        return userMapper.toDTO(savedUser);
    }

    /**
     * Delete user (soft delete by disabling)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        
        User user = findUserById(id);
        user.setIsEnabled(false); // ✅ setIsEnabled correto
        
        userRepository.save(user);
        log.info("User deleted (disabled) successfully: {}", user.getUsername());
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        log.info("Changing password for user id: {}", id);
        
        if (!request.isNewPasswordMatching()) {
            throw new BusinessException("New passwords do not match");
        }
        
        User user = findUserById(id);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Add role to user
     */
    @Transactional
    public UserDTO addRoleToUser(Long userId, String roleName) {
        log.info("Adding role {} to user id: {}", roleName, userId);
        
        User user = findUserById(userId);
        Role role = roleRepository.findByName(roleName) // ✅ String correto
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
        
        user.addRole(role);
        User savedUser = userRepository.save(user);
        
        log.info("Role {} added successfully to user: {}", roleName, user.getUsername());
        return userMapper.toDTO(savedUser);
    }

    /**
     * Remove role from user
     */
    @Transactional
    public UserDTO removeRoleFromUser(Long userId, String roleName) {
        log.info("Removing role {} from user id: {}", roleName, userId);
        
        User user = findUserById(userId);
        Role role = roleRepository.findByName(roleName) // ✅ String correto
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
        
        user.removeRole(role);
        User savedUser = userRepository.save(user);
        
        log.info("Role {} removed successfully from user: {}", roleName, user.getUsername());
        return userMapper.toDTO(savedUser);
    }

    /**
     * Get users by role
     */
    public List<UserDTO> getUsersByRole(String roleName) {
        log.info("Fetching users by role: {}", roleName);
        List<User> users = userRepository.findByRoleName(roleName);
        return userMapper.toDTOList(users);
    }

    /**
     * Search users
     */
    public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(userMapper::toDTO);
    }

    /**
     * Get enabled users only
     */
    public List<UserDTO> getEnabledUsers() {
        log.info("Fetching enabled users");
        List<User> users = userRepository.findByIsEnabledTrue();
        return userMapper.toDTOList(users);
    }

    /**
     * Get users with failed login attempts
     */
    public List<UserDTO> getUsersWithFailedLogins(int threshold) {
        log.info("Fetching users with failed login attempts > {}", threshold);
        List<User> users = userRepository.findByFailedLoginAttemptsGreaterThan(threshold);
        return userMapper.toDTOList(users);
    }

    /**
     * Get all active users
     */
    public List<UserDTO> getAllActiveUsers() {
        log.info("Fetching all active users");
        List<User> users = userRepository.findByIsEnabledTrue();
        return userMapper.toDTOList(users);
    }

    /**
     * Update user (overloaded for UserDTO)
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {} using UserDTO", id);
        
        User existingUser = findUserById(id);
        
        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
            userRepository.existsByUsernameAndIdNot(userDTO.getUsername(), id)) {
            throw new BusinessException("Username already exists: " + userDTO.getUsername());
        }
        
        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
            userRepository.existsByEmailAndIdNot(userDTO.getEmail(), id)) {
            throw new BusinessException("Email already exists: " + userDTO.getEmail());
        }
        
        // Update fields from DTO
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setIsEnabled(userDTO.getIsEnabled());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully using DTO: {}", updatedUser.getUsername());
        
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Change user password (overloaded for separate parameters)
     */
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        log.info("Changing password for user id: {} with separate parameters", id);
        
        User user = findUserById(id);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Deactivate user (alias for disable)
     */
    @Transactional
    public UserDTO deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        return disableUser(id); // Usa o método disable existente
    }

    // ========== HELPER METHODS ==========

    /**
     * Find user by ID or throw exception
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found with id: " + id));
    }

    /**
     * Check if user exists
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Count users by role
     */
    public long countUsersByRole(String roleName) {
        return userRepository.countByRoleName(roleName);
    }

    public List<UserDTO> getRecentlyActiveUsers(LocalDateTime since) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRecentlyActiveUsers'");
    }
}
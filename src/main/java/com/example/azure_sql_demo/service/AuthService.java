// AuthService.java - CORRIGIDO
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.dto.AuthRequest;
import com.example.azure_sql_demo.dto.AuthResponse;
import com.example.azure_sql_demo.dto.UserRegistrationRequest;
import com.example.azure_sql_demo.exception.BusinessException;
import com.example.azure_sql_demo.mapper.UserMapper;
import com.example.azure_sql_demo.model.Role;
import com.example.azure_sql_demo.model.User;
import com.example.azure_sql_demo.repository.RoleRepository;
import com.example.azure_sql_demo.repository.UserRepository;
import com.example.azure_sql_demo.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;

    /**
     * Authenticate user and return JWT token
     */
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByUsernameOrEmail(request.getUsername())
                    .orElseThrow(() -> new BusinessException("User not found"));

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Generate JWT token
            String token = jwtTokenUtil.generateToken(user.getUsername());

            // Get user roles as Set<String>
            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            log.info("User authenticated successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles) // ✅ Set<String> correto
                    .expiresIn(jwtTokenUtil.getJwtExpiration())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            
            // Update failed login attempts
            userRepository.findByUsernameOrEmail(request.getUsername())
                    .ifPresent(user -> {
                        user.incrementFailedLoginAttempts();
                        
                        // Lock account after 5 failed attempts
                        if (user.getFailedLoginAttempts() >= 5) {
                            user.setIsAccountNonLocked(false);
                            log.warn("Account locked for user: {}", user.getUsername());
                        }
                        
                        userRepository.save(user);
                    });
            
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
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
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        // Generate JWT token for auto-login
        String token = jwtTokenUtil.generateToken(savedUser.getUsername());
        
        // Get user roles as Set<String>
        Set<String> roles = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(roles) // ✅ Set<String> correto
                .expiresIn(jwtTokenUtil.getJwtExpiration())
                .build();
    }

    /**
     * Refresh JWT token
     */
    public AuthResponse refreshToken(String token) {
        log.info("Refreshing JWT token");
        
        try {
            // Extract username from token
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("User not found"));
            
            // Check if user is still active
            if (!user.getIsEnabled()) {
                throw new BusinessException("User account is disabled");
            }
            
            // Generate new token
            String newToken = jwtTokenUtil.generateToken(user.getUsername());
            
            // Get user roles as Set<String>
            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            
            log.info("Token refreshed successfully for user: {}", username);
            
            return AuthResponse.builder()
                    .token(newToken)
                    .type("Bearer")
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles) // ✅ Set<String> correto
                    .expiresIn(jwtTokenUtil.getJwtExpiration())
                    .build();
            
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new BusinessException("Invalid or expired token");
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username).orElse(null);
            
            return user != null && 
                   user.getIsEnabled() && 
                   user.getIsAccountNonLocked() &&
                   jwtTokenUtil.validateToken(token, username);
                   
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Logout user (invalidate token on client side)
     */
    public void logout(String username) {
        log.info("User logged out: {}", username);
        // In a real application, you might want to blacklist the token
        // or store logout time for additional security
    }

    /**
     * Reset password (basic implementation)
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setIsAccountNonLocked(true);
        
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    /**
     * Change password for authenticated user
     */
    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockAccount(String username) {
        log.info("Unlocking account for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        user.setIsAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        
        log.info("Account unlocked successfully for user: {}", username);
    }
}

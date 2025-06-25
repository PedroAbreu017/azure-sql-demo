// UserDetailsServiceImpl.java - CORRIGIDO
package com.example.azure_sql_demo.security;

import com.example.azure_sql_demo.model.User;
import com.example.azure_sql_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.debug("User found: {}, enabled: {}, locked: {}", 
                user.getUsername(), user.getIsEnabled(), !user.getIsAccountNonLocked());

        // ✅ Usar constructor em vez de builder
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getIsEnabled(), // ✅ getIsEnabled correto
                user.getIsAccountNonExpired(),
                user.getIsAccountNonLocked(),
                user.getIsCredentialsNonExpired(),
                getAuthorities(user)
        );
    }

    /**
     * Convert user roles to Spring Security authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // ✅ getName() retorna String
                .collect(Collectors.toSet());
    }
}
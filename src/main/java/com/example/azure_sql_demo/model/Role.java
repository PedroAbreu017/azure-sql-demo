// Role.java - CORRIGIDO
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = "users") // Evita recursão infinita
@ToString(exclude = "users") // Evita recursão infinita
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 20, message = "Role name must be between 2 and 20 characters")
    private String name;
    
    @Column(length = 255)
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Relationships
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;
    
    // Business methods
    public void addUser(User user) {
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(user);
        user.addRole(this);
    }
    
    public void removeUser(User user) {
        if (users != null) {
            users.remove(user);
            user.removeRole(this);
        }
    }
}
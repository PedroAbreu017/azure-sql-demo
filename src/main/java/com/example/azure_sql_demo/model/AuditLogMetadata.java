// AuditLogMetadata.java (Base class for common audit fields)
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode
public abstract class AuditLogMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "old_values", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Default constructor for JPA
    protected AuditLogMetadata() {}

    // Business methods
    public boolean isCreateAction() {
        return "CREATE".equals(action);
    }

    public boolean isUpdateAction() {
        return "UPDATE".equals(action);
    }

    public boolean isDeleteAction() {
        return "DELETE".equals(action);
    }

    public boolean hasChanges() {
        return (oldValues != null && !oldValues.trim().isEmpty()) ||
               (newValues != null && !newValues.trim().isEmpty());
    }
}
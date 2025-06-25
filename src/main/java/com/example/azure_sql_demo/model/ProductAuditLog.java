// ProductAuditLog.java
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_audit_log", indexes = {
    @Index(name = "idx_product_audit_product_id", columnList = "product_id"),
    @Index(name = "idx_product_audit_action", columnList = "action"),
    @Index(name = "idx_product_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_product_audit_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"product", "user"})
@ToString(exclude = {"product", "user"})
public class ProductAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action cannot exceed 50 characters")
    private String action;

    @Column(name = "old_values", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "NVARCHAR(MAX)")
    private String newValues;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    @Size(max = 500, message = "User agent cannot exceed 500 characters")
    private String userAgent;

    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relationships (optional - for easier querying)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

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

    public boolean isStockUpdateAction() {
        return "STOCK_UPDATE".equals(action);
    }

    public boolean hasOldValues() {
        return oldValues != null && !oldValues.trim().isEmpty();
    }

    public boolean hasNewValues() {
        return newValues != null && !newValues.trim().isEmpty();
    }

    // Enum for common actions
    public enum AuditAction {
        CREATE("CREATE"),
        UPDATE("UPDATE"),
        DELETE("DELETE"),
        ACTIVATE("ACTIVATE"),
        DEACTIVATE("DEACTIVATE"),
        STOCK_UPDATE("STOCK_UPDATE"),
        PRICE_UPDATE("PRICE_UPDATE");

        private final String value;

        AuditAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

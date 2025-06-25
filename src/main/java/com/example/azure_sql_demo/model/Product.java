// Product.java - CORRIGIDO
package com.example.azure_sql_demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;
    
    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;
    
    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @Column(length = 50)
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;
    
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;
    
    // Business methods
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }
    
    public void decreaseQuantity(int amount) {
        if (quantity == null || quantity < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        this.quantity -= amount;
    }
    
    public void increaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.quantity = (this.quantity == null ? 0 : this.quantity) + amount;
    }
}


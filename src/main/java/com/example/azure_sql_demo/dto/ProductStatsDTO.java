// ProductStatsDTO.java - DTO para estatísticas
package com.example.azure_sql_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsDTO {
    
    private Long totalProducts;
    private Long activeProducts;
    private Long inactiveProducts;
    private Long outOfStockProducts;
    private Long lowStockProducts;
    private BigDecimal totalInventoryValue;
    private BigDecimal averagePrice;
    private String mostExpensiveProductName;
    private String cheapestProductName;
    private String topCategory;
    private Integer totalCategories;
}
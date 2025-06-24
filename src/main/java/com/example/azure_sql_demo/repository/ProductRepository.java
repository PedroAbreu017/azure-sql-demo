// ProductRepository.java - CORRIGIDO
package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ========== BASIC QUERIES ==========

    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by name containing (case-insensitive)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find active products only
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find inactive products only
     */
    List<Product> findByIsActiveFalse();

    // ========== STOCK RELATED QUERIES ==========

    /**
     * Find products with low stock (active only)
     */
    List<Product> findByQuantityLessThanAndIsActiveTrue(int threshold);

    /**
     * Find products with exact quantity (active only)
     */
    List<Product> findByQuantityEqualsAndIsActiveTrue(int quantity);

    /**
     * Find out of stock products (quantity = 0, active only)
     */
    @Query("SELECT p FROM Product p WHERE p.quantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    // ========== VALIDATION QUERIES ==========

    /**
     * Check if product exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if product exists by name excluding specific ID
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // ========== COUNTING QUERIES ==========

    /**
     * Count active products
     */
    long countByIsActiveTrue();

    /**
     * Count inactive products
     */
    long countByIsActiveFalse();

    /**
     * Count products by category
     */
    long countByCategory(String category);

    /**
     * Count products with specific quantity (active only)
     */
    long countByQuantityEqualsAndIsActiveTrue(int quantity);

    /**
     * Count products with quantity less than threshold (active only)
     */
    long countByQuantityLessThanAndIsActiveTrue(int threshold);

    // ========== CATEGORY QUERIES ==========

    /**
     * Find distinct categories
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();

    /**
     * Count distinct categories
     */
    @Query("SELECT COUNT(DISTINCT p.category) FROM Product p WHERE p.category IS NOT NULL")
    Integer countDistinctCategories();

    /**
     * Find top category by product count
     */
    @Query("SELECT p.category FROM Product p WHERE p.category IS NOT NULL " +
           "GROUP BY p.category ORDER BY COUNT(p) DESC")
    Optional<String> findTopCategoryByProductCount();

    // ========== PRICE QUERIES ==========

    /**
     * Find most expensive active product
     */
    Optional<Product> findTopByIsActiveTrueOrderByPriceDesc();

    /**
     * Find cheapest active product
     */
    Optional<Product> findTopByIsActiveTrueOrderByPriceAsc();

    /**
     * Find products with price greater than
     */
    List<Product> findByPriceGreaterThanAndIsActiveTrue(BigDecimal price);

    /**
     * Find products with price less than
     */
    List<Product> findByPriceLessThanAndIsActiveTrue(BigDecimal price);

    // ========== ADVANCED QUERIES ==========

    /**
     * Find products by multiple criteria
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.quantity > 0) OR (:inStock = false AND p.quantity = 0))")
    Page<Product> findProductsByCriteria(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isActive") Boolean isActive,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Find top products by value (price * quantity)
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "ORDER BY (p.price * p.quantity) DESC")
    List<Product> findTopProductsByValue(Pageable pageable);

    /**
     * Calculate total inventory value
     */
    @Query("SELECT SUM(p.price * p.quantity) FROM Product p WHERE p.isActive = true")
    Optional<BigDecimal> calculateTotalInventoryValue();

    /**
     * Calculate average price of active products
     */
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    Optional<BigDecimal> calculateAveragePrice();

    // ========== REPORTING QUERIES ==========

    /**
     * Product count by category
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p " +
           "WHERE p.category IS NOT NULL " +
           "GROUP BY p.category " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> getProductCountByCategory();

    /**
     * Products needing restock (quantity below threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold AND p.isActive = true " +
           "ORDER BY p.quantity ASC")
    List<Product> findProductsNeedingRestock(@Param("threshold") int threshold);

    /**
     * Recently created products
     */
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Product> findRecentlyCreatedProducts(@Param("since") java.time.LocalDateTime since);

    /**
     * Recently updated products
     */
    @Query("SELECT p FROM Product p WHERE p.updatedAt >= :since ORDER BY p.updatedAt DESC")
    List<Product> findRecentlyUpdatedProducts(@Param("since") java.time.LocalDateTime since);

    // ========== CUSTOM NATIVE QUERIES ==========

    /**
     * Get products with low stock using native query for performance
     */
    @Query(value = "SELECT * FROM products WHERE quantity < ?1 AND is_active = 1 " +
                   "ORDER BY quantity ASC, name ASC", nativeQuery = true)
    List<Product> findLowStockProductsNative(int threshold);

    /**
     * Get product statistics using native query
     */
    @Query(value = "SELECT " +
                   "COUNT(*) as total_products, " +
                   "SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_products, " +
                   "SUM(CASE WHEN quantity = 0 AND is_active = 1 THEN 1 ELSE 0 END) as out_of_stock, " +
                   "SUM(CASE WHEN quantity < 10 AND is_active = 1 THEN 1 ELSE 0 END) as low_stock, " +
                   "SUM(CASE WHEN is_active = 1 THEN price * quantity ELSE 0 END) as total_value " +
                   "FROM products", nativeQuery = true)
    Object[] getProductStatisticsNative();

    // ========== BULK OPERATIONS ==========

    /**
     * Update stock for multiple products
     */
    @Query("UPDATE Product p SET p.quantity = p.quantity + :adjustment " +
           "WHERE p.id IN :productIds")
    int bulkUpdateStock(@Param("productIds") List<Long> productIds, 
                       @Param("adjustment") int adjustment);

    /**
     * Deactivate products by category
     */
    @Query("UPDATE Product p SET p.isActive = false WHERE p.category = :category")
    int deactivateProductsByCategory(@Param("category") String category);

    /**
     * Update prices by percentage
     */
    @Query("UPDATE Product p SET p.price = p.price * (1 + :percentageChange / 100.0) " +
           "WHERE p.category = :category AND p.isActive = true")
    int updatePricesByCategory(@Param("category") String category, 
                              @Param("percentageChange") double percentageChange);
}
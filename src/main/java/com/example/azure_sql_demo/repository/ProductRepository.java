package com.example.azure_sql_demo.repository;

import com.example.azure_sql_demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

  List<Product> findByCategory(String category);

  List<Product> findByNameContainingIgnoreCase(String name);

  List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

  @Query("SELECT p FROM Product p WHERE p.quantity > 0 ORDER BY p.createdAt DESC")
  List<Product> findAvailableProducts();

  @Query(value = "SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice", nativeQuery = true)
  List<Product> findProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
  
}

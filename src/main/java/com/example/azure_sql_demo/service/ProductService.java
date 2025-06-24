// ProductService.java - CORRIGIDO
package com.example.azure_sql_demo.service;

import com.example.azure_sql_demo.dto.CreateProductRequest;
import com.example.azure_sql_demo.dto.ProductDTO;
import com.example.azure_sql_demo.dto.ProductStatsDTO;
import com.example.azure_sql_demo.dto.UpdateProductRequest;
import com.example.azure_sql_demo.exception.BusinessException;
import com.example.azure_sql_demo.mapper.ProductMapper;
import com.example.azure_sql_demo.model.Product;
import com.example.azure_sql_demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AuditService auditService;

    /**
     * Retrieves all products with pagination
     */
    @Cacheable(value = "products", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: {}", pageable);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toDTO);
    }

    /**
     * Retrieves a product by ID
     */
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by id: {}", id);
        Product product = findProductById(id);
        return productMapper.toDTO(product);
    }

    /**
     * Creates a new product
     */
    @Transactional
    @CacheEvict(value = {"products", "product-stats"}, allEntries = true)
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        
        // Validate business rules
        validateProductCreation(request);
        
        // Convert DTO to entity
        Product product = productMapper.toEntity(request);
        
        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        
        // Audit log
        auditService.logProductCreation(savedProduct);
        
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Updates an existing product
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "product-stats"}, allEntries = true)
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);
        
        Product existingProduct = findProductById(id);
        Product originalProduct = copyProduct(existingProduct);
        
        // Update fields from request
        updateProductFields(existingProduct, request);
        
        // Validate business rules
        validateProductUpdate(existingProduct, request);
        
        // Save updated product
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", updatedProduct.getId());
        
        // Audit log
        auditService.logProductUpdate(originalProduct, updatedProduct);
        
        return productMapper.toDTO(updatedProduct);
    }

    /**
     * Soft deletes a product (marks as inactive)
     */
    @Transactional
    @CacheEvict(value = {"products", "product", "product-stats"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        Product product = findProductById(id);
        
        if (!product.getIsActive()) {
            throw new BusinessException("Product is already inactive");
        }
        
        product.setIsActive(false);
        productRepository.save(product);
        
        log.info("Product soft deleted successfully: {}", id);
        auditService.logProductDeletion(product);
    }

    /**
     * Retrieves products by category
     */
    @Cacheable(value = "products-by-category", key = "#category")
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        return productMapper.toDTOList(products);
    }

    /**
     * Searches products by name (case-insensitive)
     */
    public List<ProductDTO> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toDTOList(products);
    }

    /**
     * Retrieves products within a price range
     */
    public List<ProductDTO> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching products in price range: {} - {}", minPrice, maxPrice);
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return productMapper.toDTOList(products);
    }

    /**
     * Retrieves products with low stock
     */
    public List<ProductDTO> getLowStockProducts(int threshold) {
        log.info("Fetching products with stock below: {}", threshold);
        List<Product> products = productRepository.findByQuantityLessThanAndIsActiveTrue(threshold);
        return productMapper.toDTOList(products);
    }

    /**
     * Retrieves products that are out of stock
     */
    public List<ProductDTO> getOutOfStockProducts() {
        log.info("Fetching out of stock products");
        List<Product> products = productRepository.findByQuantityEqualsAndIsActiveTrue(0);
        return productMapper.toDTOList(products);
    }

    /**
     * Updates product stock quantity
     */
    @Transactional
    @CacheEvict(value = {"products", "product"}, key = "#id")
    public ProductDTO updateProductStock(Long id, Integer quantity) {
        log.info("Updating stock for product id: {} to quantity: {}", id, quantity);
        
        Product product = findProductById(id);
        Integer oldQuantity = product.getQuantity();
        
        product.setQuantity(quantity);
        Product savedProduct = productRepository.save(product);
        
        log.info("Stock updated from {} to {} for product: {}", oldQuantity, quantity, id);
        auditService.logStockUpdate(product, oldQuantity, quantity);
        
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Activates a product
     */
    @Transactional
    @CacheEvict(value = {"products", "product"}, key = "#id")
    public ProductDTO activateProduct(Long id) {
        log.info("Activating product with id: {}", id);
        
        Product product = findProductById(id);
        
        if (product.getIsActive()) {
            throw new BusinessException("Product is already active");
        }
        
        product.setIsActive(true);
        Product savedProduct = productRepository.save(product);
        
        log.info("Product activated successfully: {}", id);
        auditService.logProductActivation(product);
        
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Deactivates a product
     */
    @Transactional
    @CacheEvict(value = {"products", "product"}, key = "#id")
    public ProductDTO deactivateProduct(Long id) {
        log.info("Deactivating product with id: {}", id);
        
        Product product = findProductById(id);
        
        if (!product.getIsActive()) {
            throw new BusinessException("Product is already inactive");
        }
        
        product.setIsActive(false);
        Product savedProduct = productRepository.save(product);
        
        log.info("Product deactivated successfully: {}", id);
        auditService.logProductDeactivation(product);
        
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Retrieves all product categories
     */
    @Cacheable(value = "product-categories")
    public List<String> getAllCategories() {
        log.info("Fetching all product categories");
        return productRepository.findDistinctCategories();
    }

    /**
     * Retrieves product statistics
     */
    @Cacheable(value = "product-stats")
    public ProductStatsDTO getProductStatistics() {
        log.info("Calculating product statistics");
        
        Long totalProducts = productRepository.count();
        Long activeProducts = productRepository.countByIsActiveTrue();
        Long inactiveProducts = productRepository.countByIsActiveFalse();
        Long outOfStockProducts = productRepository.countByQuantityEqualsAndIsActiveTrue(0);
        Long lowStockProducts = productRepository.countByQuantityLessThanAndIsActiveTrue(10);
        
        BigDecimal totalInventoryValue = calculateTotalInventoryValue();
        BigDecimal averagePrice = calculateAveragePrice();
        
        Optional<Product> mostExpensive = productRepository.findTopByIsActiveTrueOrderByPriceDesc();
        Optional<Product> cheapest = productRepository.findTopByIsActiveTrueOrderByPriceAsc();
        
        String topCategory = productRepository.findTopCategoryByProductCount()
                .orElse("No categories");
        
        Integer totalCategories = productRepository.countDistinctCategories();
        
        return ProductStatsDTO.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .outOfStockProducts(outOfStockProducts)
                .lowStockProducts(lowStockProducts)
                .totalInventoryValue(totalInventoryValue)
                .averagePrice(averagePrice)
                .mostExpensiveProductName(mostExpensive.map(Product::getName).orElse("N/A"))
                .cheapestProductName(cheapest.map(Product::getName).orElse("N/A"))
                .topCategory(topCategory)
                .totalCategories(totalCategories)
                .build();
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Finds a product by ID or throws exception
     */
    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found with id: " + id));
    }

    /**
     * Validates product creation business rules
     */
    private void validateProductCreation(CreateProductRequest request) {
        // Check if product name already exists
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Product with name '" + request.getName() + "' already exists");
        }
        
        // Validate price
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Product price must be greater than zero");
        }
        
        // Validate quantity
        if (request.getQuantity() < 0) {
            throw new BusinessException("Product quantity cannot be negative");
        }
    }

    /**
     * Validates product update business rules
     */
    private void validateProductUpdate(Product product, UpdateProductRequest request) {
        // Check if updating name and name already exists for different product
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), product.getId())) {
                throw new BusinessException("Product with name '" + request.getName() + "' already exists");
            }
        }
        
        // Validate price if provided
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Product price must be greater than zero");
        }
        
        // Validate quantity if provided
        if (request.getQuantity() != null && request.getQuantity() < 0) {
            throw new BusinessException("Product quantity cannot be negative");
        }
    }

    /**
     * Updates product fields from request
     */
    private void updateProductFields(Product product, UpdateProductRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
    }

    /**
     * Creates a copy of product for audit purposes
     */
    private Product copyProduct(Product original) {
        return Product.builder()
                .id(original.getId())
                .name(original.getName())
                .description(original.getDescription())
                .price(original.getPrice())
                .quantity(original.getQuantity())
                .category(original.getCategory())
                .isActive(original.getIsActive())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .createdBy(original.getCreatedBy())
                .lastModifiedBy(original.getLastModifiedBy())
                .build();
    }

    /**
     * Calculates total inventory value
     */
    private BigDecimal calculateTotalInventoryValue() {
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        return activeProducts.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates average price of active products
     */
    private BigDecimal calculateAveragePrice() {
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        
        if (activeProducts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPrice = activeProducts.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalPrice.divide(BigDecimal.valueOf(activeProducts.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if product exists by ID
     */
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Checks if product is active
     */
    public boolean isProductActive(Long id) {
        return productRepository.findById(id)
                .map(Product::getIsActive)
                .orElse(false);
    }

    /**
     * Gets active products count
     */
    public long getActiveProductsCount() {
        return productRepository.countByIsActiveTrue();
    }

    /**
     * Gets products count by category
     */
    public long getProductsCountByCategory(String category) {
        return productRepository.countByCategory(category);
    }
}

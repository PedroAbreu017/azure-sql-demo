
// ProductMapper.java - CORRIGIDO
package com.example.azure_sql_demo.mapper;

import com.example.azure_sql_demo.dto.CreateProductRequest;
import com.example.azure_sql_demo.dto.ProductDTO;
import com.example.azure_sql_demo.dto.UpdateProductRequest;
import com.example.azure_sql_demo.model.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    /**
     * Convert Product entity to ProductDTO (PRIMARY MAPPING)
     */
    ProductDTO toDTO(Product product);

    /**
     * Convert list of Product entities to list of ProductDTOs
     * Uses the primary toDTO method to avoid ambiguity
     */
    @IterableMapping(qualifiedByName = "productToDTO")
    List<ProductDTO> toDTOList(List<Product> products);

    /**
     * Primary mapping method with qualifier
     */
    @Named("productToDTO")
    default ProductDTO productToDTO(Product product) {
        return toDTO(product);
    }

    /**
     * Convert CreateProductRequest to Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Product toEntity(CreateProductRequest request);

    /**
     * Update Product entity from UpdateProductRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    /**
     * Create a minimal ProductDTO with only essential fields
     */
    @Named("toMinimalDTO")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductDTO toMinimalDTO(Product product);

    /**
     * Create a summary ProductDTO for listing purposes  
     */
    @Named("toSummaryDTO")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductDTO toSummaryDTO(Product product);
}
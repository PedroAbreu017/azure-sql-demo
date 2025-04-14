package com.example.azure_sql_demo.controller;

import com.example.azure_sql_demo.model.Product;
import com.example.azure_sql_demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class DataLoaderController {

    @Autowired
    private ProductRepository productRepository;
    
    private final Random random = new Random();
    
    // Endpoint para carregar dados de amostra
    @PostMapping("/load-sample-data")
    public ResponseEntity<?> loadSampleData(@RequestParam(defaultValue = "20") int count) {
        List<Product> products = generateSampleProducts(count);
        productRepository.saveAll(products);
        
        return ResponseEntity.ok().body(
            Map.of("message", "Successfully loaded " + products.size() + " sample products")
        );
    }
    
    // Endpoint para limpar todos os dados
    @DeleteMapping("/clear-data")
    public ResponseEntity<?> clearAllData() {
        long count = productRepository.count();
        productRepository.deleteAll();
        
        return ResponseEntity.ok().body(
            Map.of("message", "Successfully deleted " + count + " products")
        );
    }
    
    // Endpoint para carregar dados predefinidos
    @PostMapping("/load-predefined-data")
    public ResponseEntity<?> loadPredefinedData() {
        List<Product> products = createPredefinedProducts();
        productRepository.saveAll(products);
        
        return ResponseEntity.ok().body(
            Map.of("message", "Successfully loaded " + products.size() + " predefined products")
        );
    }
    
    // Método auxiliar para gerar produtos aleatórios
    private List<Product> generateSampleProducts(int count) {
        List<Product> products = new ArrayList<>();
        
        // Categorias de exemplo
        String[] categories = {"Electronics", "Clothing", "Books", "Home", "Sports", "Beauty", "Toys", "Food"};
        String[] adjectives = {"Premium", "Deluxe", "Advanced", "Professional", "Ultimate", "Essential", "Basic"};
        String[] items = {"Laptop", "Smartphone", "Headphones", "Camera", "Watch", "Speaker", "Tablet", "Monitor"};
        
        // Gerar produtos aleatórios
        for (int i = 1; i <= count; i++) {
            String adjective = adjectives[random.nextInt(adjectives.length)];
            String item = items[random.nextInt(items.length)];
            
            Product product = new Product();
            product.setName(adjective + " " + item);
            product.setDescription("High-quality " + item.toLowerCase() + " with exceptional features");
            product.setPrice(new BigDecimal(50 + random.nextInt(950)));
            product.setQuantity(1 + random.nextInt(100));
            product.setCategory(categories[random.nextInt(categories.length)]);
            product.setCreatedAt(LocalDateTime.now());
            
            products.add(product);
        }
        
        return products;
    }
    
    // Método para criar produtos predefinidos específicos
    private List<Product> createPredefinedProducts() {
        List<Product> products = new ArrayList<>();
        
        // Eletrônicos
        Product laptop = new Product();
        laptop.setName("MacBook Pro");
        laptop.setDescription("Apple MacBook Pro com chip M2, 16GB RAM, 512GB SSD");
        laptop.setPrice(new BigDecimal("1999.99"));
        laptop.setQuantity(10);
        laptop.setCategory("Electronics");
        laptop.setCreatedAt(LocalDateTime.now());
        products.add(laptop);
        
        Product smartphone = new Product();
        smartphone.setName("iPhone 15 Pro");
        smartphone.setDescription("Apple iPhone 15 Pro com 256GB de armazenamento");
        smartphone.setPrice(new BigDecimal("1199.99"));
        smartphone.setQuantity(15);
        smartphone.setCategory("Electronics");
        smartphone.setCreatedAt(LocalDateTime.now());
        products.add(smartphone);
        
        // Roupas
        Product jacket = new Product();
        jacket.setName("Jaqueta de Couro");
        jacket.setDescription("Jaqueta de couro genuíno para homens e mulheres");
        jacket.setPrice(new BigDecimal("299.99"));
        jacket.setQuantity(8);
        jacket.setCategory("Clothing");
        jacket.setCreatedAt(LocalDateTime.now());
        products.add(jacket);
        
        // Livros
        Product book = new Product();
        book.setName("Arquitetura Limpa");
        book.setDescription("Livro sobre arquitetura de software por Robert C. Martin");
        book.setPrice(new BigDecimal("49.99"));
        book.setQuantity(25);
        book.setCategory("Books");
        book.setCreatedAt(LocalDateTime.now());
        products.add(book);
        
        // Esportes
        Product tennisBall = new Product();
        tennisBall.setName("Bolas de Tênis");
        tennisBall.setDescription("Conjunto com 3 bolas de tênis de alta qualidade");
        tennisBall.setPrice(new BigDecimal("12.99"));
        tennisBall.setQuantity(50);
        tennisBall.setCategory("Sports");
        tennisBall.setCreatedAt(LocalDateTime.now());
        products.add(tennisBall);
        
        return products;
    }
}
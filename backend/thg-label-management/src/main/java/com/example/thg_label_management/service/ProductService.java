package com.example.thg_label_management.service;

import com.example.thg_label_management.model.Product;
import com.example.thg_label_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Product metadata and relationships.
 * Handles Master/Child linking and product attribute persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Persists or updates a product in the local database.
     * This is used for both initial catalogue ingestion and manual attribute overrides.
     *
     * @param product The product entity to save.
     * @return The persists Product entity.
     */
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProduct(String sku) {
        return productRepository.findById(sku);
    }

    /**
     * Searches for products across multiple fields (SKU, Title, Barcode, Catalogue Number).
     * prioritizes exact matches for SKU and barcode.
     *
     * @param query The search string provided by the user.
     * @return A list of matching Product entities.
     */
    public List<Product> searchProducts(String query) {
        log.info("Searching for products with query: {}", query);
        
        // Simple search logic
        List<Product> byTitle = productRepository.findByTitleContainingIgnoreCase(query);
        if (!byTitle.isEmpty()) {
            log.debug("Found {} products by title", byTitle.size());
            return byTitle;
        }
        
        List<Product> byBarcode = productRepository.findByBarcode(query);
        if (!byBarcode.isEmpty()) {
            log.debug("Found {} products by barcode", byBarcode.size());
            return byBarcode;
        }

        List<Product> byCatalogue = productRepository.findByCatalogueNumber(query);
        if (!byCatalogue.isEmpty()) {
            log.debug("Found {} products by catalogue number", byCatalogue.size());
            return byCatalogue;
        }

        List<Product> bySku = productRepository.findAll().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(query))
                .toList();

        log.debug("Found {} products by SKU exact match", bySku.size());
        return bySku;
    }

    public List<Product> getChildProducts(String masterSku) {
        return productRepository.findByMasterSku(masterSku);
    }
}

package com.example.thg_label_management.repository;

import com.example.thg_label_management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByTitleContainingIgnoreCase(String title);
    List<Product> findByBarcode(String barcode);
    List<Product> findByCatalogueNumber(String catalogueNumber);
    List<Product> findByMasterSku(String masterSku);
}

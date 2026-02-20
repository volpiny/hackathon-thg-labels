package com.example.thg_label_management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Product {
    @Id
    private String sku; // Using SKU as ID
    
    private String title;
    private String barcode;
    private String catalogueNumber;
    
    private String category; // Food, Supplement
    private String type; // Solid, Liquid, Powder
    
    @ElementCollection
    private List<String> marketTerritories; // EU, Australia, India
    
    private boolean masterProduct;
    
    private String masterSku; // Reference to master if this is a child
    
    @OneToMany(mappedBy = "sku", fetch = FetchType.LAZY)
    private List<Label> labels;
}

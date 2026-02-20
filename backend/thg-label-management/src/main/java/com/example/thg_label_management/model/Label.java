package com.example.thg_label_management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sku_id")
    private String sku;
    
    private Integer version;
    private String fileName;
    private String s3Key; // Path in MinIO
    private boolean active;
    private boolean deleted = false;
    private Boolean skuMatched;
    
    private LocalDateTime createdAt;
    private String createdBy; // "Dummy User"
}

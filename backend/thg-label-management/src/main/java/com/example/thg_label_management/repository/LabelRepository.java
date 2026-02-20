package com.example.thg_label_management.repository;

import com.example.thg_label_management.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    
    Optional<Label> findFirstBySkuAndDeletedFalseOrderByVersionDesc(String sku);
    
    List<Label> findBySkuAndActiveTrueAndDeletedFalse(String sku);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Label l SET l.active = false WHERE l.sku = :sku AND l.active = true AND l.deleted = false")
    void deactivateAllBySku(String sku);

    List<Label> findBySkuAndDeletedFalseOrderByVersionDesc(String sku);
}

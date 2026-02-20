package com.example.thg_label_management.service;

import com.example.thg_label_management.model.Product;
import com.example.thg_label_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating operational readiness metrics.
 * Aggregates data from the ProductRepository to provide high-level insights
 * into label coverage and product distribution.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;

    /**
     * Calculates and returns operational statistics for the dashboard.
     * Includes total products, ready products (those with active labels),
     * readiness percentage, and category-based distribution.
     *
     * @return A map containing calculated metrics.
     */
    public Map<String, Object> getStats() {
        List<Product> products = productRepository.findAll();
        long totalProducts = products.size();
        long readyProducts = products.stream()
                .filter(p -> p.getLabels() != null && p.getLabels().stream().anyMatch(l -> l.isActive()))
                .count();

        Map<String, Long> categoryCount = products.stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("readyProducts", readyProducts);
        stats.put("readinessPercentage", totalProducts > 0 ? (double) readyProducts / totalProducts * 100 : 0);
        stats.put("categoryDistribution", categoryCount);

        return stats;
    }
}

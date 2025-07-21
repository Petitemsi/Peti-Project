package com.peti.service;

import com.peti.constants.Season;
import com.peti.dto.CategoryUsageDto;
import com.peti.model.Category;
import com.peti.model.ClothingItem;
import com.peti.repository.CategoryRepository;
import com.peti.repository.ClothingItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ClothingItemRepository clothingItemRepository;

    public List<Category> findAll() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setActive(false);
        categoryRepository.save(category);
    }

    public List<CategoryUsageDto> getTopUsedCategories(int limit) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<ClothingItem> items = clothingItemRepository.findByLastUsedDateAfterAndIsActiveTrue(thirtyDaysAgo);

        // Aggregate usage count by category
        Map<Category, Long> categoryUsage = items.stream()
                .filter(item -> item.getCategory() != null)
                .collect(Collectors.groupingBy(
                        ClothingItem::getCategory,
                        Collectors.summingLong(ClothingItem::getUsageCount)
                ));

        // Sort by usage count (descending) and limit to top N
        return categoryUsage.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new CategoryUsageDto(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<CategoryUsageDto> getLeastUsedCategories(int limit) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<ClothingItem> items = clothingItemRepository.findByLastUsedDateAfterAndIsActiveTrue(thirtyDaysAgo);

        // Get all active categories
        List<Category> allCategories = categoryRepository.findByIsActiveTrue();

        // Aggregate usage count by category
        Map<Category, Long> categoryUsage = items.stream()
                .filter(item -> item.getCategory() != null)
                .collect(Collectors.groupingBy(
                        ClothingItem::getCategory,
                        Collectors.summingLong(ClothingItem::getUsageCount)
                ));

        // Include categories with zero usage
        allCategories.forEach(category ->
                categoryUsage.putIfAbsent(category, 0L)
        );

        // Sort by usage count (ascending) and limit to top N
        return categoryUsage.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(limit)
                .map(entry -> new CategoryUsageDto(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Map<Season, List<CategoryUsageDto>> getCategoryUsageBySeason() {
        List<ClothingItem> items = clothingItemRepository.findByIsActiveTrue();

        // Group by season and category
        Map<Season, Map<Category, Long>> usageBySeason = items.stream()
                .filter(item -> item.getCategory() != null && item.getSeason() != null)
                .collect(Collectors.groupingBy(
                        ClothingItem::getSeason,
                        Collectors.groupingBy(
                                ClothingItem::getCategory,
                                Collectors.summingLong(ClothingItem::getUsageCount)
                        )
                ));

        // Convert to DTO format
        Map<Season, List<CategoryUsageDto>> result = new EnumMap<>(Season.class);
        for (Season season : Season.values()) {
            Map<Category, Long> categoryUsage = usageBySeason.getOrDefault(season, new HashMap<>());
            List<CategoryUsageDto> dtos = categoryUsage.entrySet().stream()
                    .map(entry -> new CategoryUsageDto(entry.getKey().getName(), entry.getValue()))
                    .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount())) // Sort by usage descending
                    .collect(Collectors.toList());
            result.put(season, dtos);
        }
        return result;
    }

}
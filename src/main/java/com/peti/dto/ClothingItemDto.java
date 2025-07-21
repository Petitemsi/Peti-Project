package com.peti.dto;

import com.peti.constants.Color;
import com.peti.constants.Occasion;
import com.peti.constants.Season;
import com.peti.model.ClothingItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClothingItemDto {
    private Long id;
    private String name;
    private String categoryName; // Changed from categoryId to categoryName
    private Color color;
    private Season season;
    private Occasion occasion;
    private String imageUrl;
    private String description;
    private LocalDate lastUsedDate;
    private int usageCount;
    private LocalDateTime createdAt;
    private boolean isActive;

    public static ClothingItemDto fromEntity(ClothingItem item) {
        return new ClothingItemDto(
                item.getId(),
                item.getName(),
                item.getCategory() != null ? item.getCategory().getName() : null, // Assuming Category has a getName() method
                item.getColor(),
                item.getSeason(),
                item.getOccasion(),
                item.getImageUrl(),
                item.getDescription(),
                item.getLastUsedDate(),
                item.getUsageCount(),
                item.getCreatedAt(),
                item.isActive()
        );
    }
}
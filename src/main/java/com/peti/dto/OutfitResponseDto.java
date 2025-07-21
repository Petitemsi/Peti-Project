package com.peti.dto;

import com.peti.constants.Occasion;
import com.peti.constants.Season;
import com.peti.model.Outfit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutfitResponseDto {
    private Long id;
    private String name;
    private Occasion occasion;
    private Season season;
    private Set<ClothingItemDto> clothes; // Updated to ClothingItemDto
    private LocalDate lastWornDate;
    private int wearCount;
    private LocalDateTime createdAt;
    private String description;
    private Long userId;

    public static OutfitResponseDto fromEntity(Outfit outfit) {
        return new OutfitResponseDto(
                outfit.getId(),
                outfit.getName(),
                outfit.getOccasion(),
                outfit.getSeason(),
                outfit.getClothes().stream()
                        .map(ClothingItemDto::fromEntity)
                        .collect(Collectors.toSet()),
                outfit.getLastWornDate(),
                outfit.getWearCount(),
                outfit.getCreatedAt(),
                outfit.getDescription(),
                outfit.getUser().getId()
        );
    }
}
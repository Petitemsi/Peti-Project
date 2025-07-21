package com.peti.service;

import com.peti.constants.Color;
import com.peti.constants.Occasion;
import com.peti.constants.Season;
import com.peti.dto.ClothingItemDto;
import com.peti.dto.ItemUsageTrendDto;
import com.peti.dto.UsagePeriodDto;
import com.peti.model.Category;
import com.peti.model.ClothingItem;
import com.peti.model.Outfit;
import com.peti.model.User;
import com.peti.repository.ClothingItemRepository;
import com.peti.repository.OutfitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardrobeService {

    private final ClothingItemRepository clothingItemRepository;
    private final OutfitRepository outfitRepository;

    public ClothingItem addItem(ClothingItem item) {
        return clothingItemRepository.save(item);
    }
    // In WardrobeService.java
    public Page<ClothingItem> getUserItems(Long userId, Pageable pageable) {
        Page<ClothingItem> items=  clothingItemRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        return items;
    }
    public List<ClothingItem> getUserItems(Long userId) {
        return clothingItemRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Optional<ClothingItem> getItemById(Long id) {
        return clothingItemRepository.findById(id);
    }

    public ClothingItem updateItem(ClothingItem item) {
        return clothingItemRepository.save(item);
    }

    public void deleteItem(Long id) {
        ClothingItem item = clothingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setActive(false);
        clothingItemRepository.save(item);
    }

    public List<ClothingItem> searchItems(User user, String searchTerm) {
        return clothingItemRepository.searchByUserAndTerm(user, searchTerm);
    }

    public List<ClothingItem> getItemsByCategory(User user, Category category) {
        return clothingItemRepository.findByUserAndCategory(user, category);
    }

    public List<ClothingItem> getItemsByColor(User user, Color color) {
        return clothingItemRepository.findByUserAndColor(user, color);
    }

    public List<ClothingItem> getItemsBySeason(User user, Season season) {
        return clothingItemRepository.findByUserAndSeason(user, season);
    }

    public List<ClothingItem> getItemsByOccasion(User user, Occasion occasion) {
        return clothingItemRepository.findByUserAndOccasion(user, occasion);
    }

    public List<ClothingItem> getUnusedItems(Long userId, LocalDate since) {
        return clothingItemRepository.findUnusedItems(userId, since);
    }

    public List<ClothingItem> getNeverUsedItems(User user) {
        return clothingItemRepository.findNeverUsedItems(user);
    }

    public void markItemAsUsed(ClothingItem item) {
        item.setLastUsedDate(LocalDate.now());
        item.setUsageCount(item.getUsageCount() + 1);
        clothingItemRepository.save(item);
    }
    public long getActiveItemsCount() {
        return clothingItemRepository.countByIsActiveTrue();
    }
    public List<UsagePeriodDto> getPeakUsagePeriods(LocalDate startDate, LocalDate endDate) {
        // Fetch clothing items used within the date range
        List<ClothingItem> items = clothingItemRepository.findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate);
        // Fetch outfits used within the date range
        List<Outfit> outfits = outfitRepository.findByLastWornDateBetweenAndIsActiveTrue(startDate, endDate);

        // Aggregate usage by day
        Map<LocalDate, Long> itemUsageByDay = items.stream()
                .filter(item -> item.getLastUsedDate() != null)
                .collect(Collectors.groupingBy(
                        ClothingItem::getLastUsedDate,
                        Collectors.summingLong(ClothingItem::getUsageCount)
                ));

        Map<LocalDate, Long> outfitUsageByDay = outfits.stream()
                .filter(outfit -> outfit.getLastWornDate() != null)
                .collect(Collectors.groupingBy(
                        Outfit::getLastWornDate,
                        Collectors.summingLong(Outfit::getWearCount)
                ));

        // Combine item and outfit usage
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    long itemCount = itemUsageByDay.getOrDefault(date, 0L);
                    long outfitCount = outfitUsageByDay.getOrDefault(date, 0L);
                    return new UsagePeriodDto(date.toString(), itemCount + outfitCount);
                })
                .collect(Collectors.toList());
    }
    public ItemUsageTrendDto getItemUsageTrends() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        // Fetch items used in the last 7 days
        List<ClothingItem> usedItems = clothingItemRepository.findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate);
        // Fetch newly added items
        List<ClothingItem> newItems = clothingItemRepository.findByCreatedAtBetweenAndIsActiveTrue(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()
        );

        // Aggregate usage by day
        Map<LocalDate, Long> usageByDay = usedItems.stream()
                .filter(item -> item.getLastUsedDate() != null)
                .collect(Collectors.groupingBy(
                        ClothingItem::getLastUsedDate,
                        Collectors.summingLong(ClothingItem::getUsageCount)
                ));

        // Create trend data
        List<UsagePeriodDto> trends = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> new UsagePeriodDto(date.toString(), usageByDay.getOrDefault(date, 0L)))
                .collect(Collectors.toList());

        // Convert new items to DTOs (assuming ClothingItemDto exists)
        List<ClothingItemDto> newItemDtos = newItems.stream()
                .map(ClothingItemDto::fromEntity)
                .collect(Collectors.toList());

        return new ItemUsageTrendDto(trends, newItemDtos);
    }
}
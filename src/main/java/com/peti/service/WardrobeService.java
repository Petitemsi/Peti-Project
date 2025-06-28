package com.peti.service;

import com.peti.model.ClothingItem;
import com.peti.model.User;
import com.peti.repository.ClothingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WardrobeService {
    
    private final ClothingItemRepository clothingItemRepository;
    
    public ClothingItem addItem(ClothingItem item) {
        return clothingItemRepository.save(item);
    }
    
    public List<ClothingItem> getUserItems(User user) {
        return clothingItemRepository.findByUserAndIsActiveTrue(user);
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
    
    public List<ClothingItem> getItemsByCategory(User user, ClothingItem.Category category) {
        return clothingItemRepository.findByUserAndCategory(user, category);
    }
    
    public List<ClothingItem> getItemsByColor(User user, ClothingItem.Color color) {
        return clothingItemRepository.findByUserAndColor(user, color);
    }
    
    public List<ClothingItem> getItemsBySeason(User user, ClothingItem.Season season) {
        return clothingItemRepository.findByUserAndSeason(user, season);
    }
    
    public List<ClothingItem> getItemsByOccasion(User user, ClothingItem.Occasion occasion) {
        return clothingItemRepository.findByUserAndOccasion(user, occasion);
    }
    
    public List<ClothingItem> getUnusedItems(User user, LocalDate since) {
        return clothingItemRepository.findUnusedItems(user, since);
    }
    
    public List<ClothingItem> getNeverUsedItems(User user) {
        return clothingItemRepository.findNeverUsedItems(user);
    }
    
    public void markItemAsUsed(ClothingItem item) {
        item.setLastUsedDate(LocalDate.now());
        item.setUsageCount(item.getUsageCount() + 1);
        clothingItemRepository.save(item);
    }
} 
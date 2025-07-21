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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WardrobeServiceTest {

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @Mock
    private OutfitRepository outfitRepository;

    @InjectMocks
    private WardrobeService wardrobeService;

    private User user;
    private ClothingItem clothingItem;
    private Outfit outfit;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        clothingItem = new ClothingItem();
        clothingItem.setId(1L);
        clothingItem.setUser(user);
        clothingItem.setName("T-Shirt");
        clothingItem.setCategory(new Category(1L, "Shirts", "Casual shirts", true));
        clothingItem.setColor(Color.BLUE);
        clothingItem.setSeason(Season.SUMMER);
        clothingItem.setOccasion(Occasion.CASUAL);
        clothingItem.setUsageCount(5);
        clothingItem.setLastUsedDate(LocalDate.now().minusDays(10));
        clothingItem.setCreatedAt(LocalDateTime.now().minusDays(5));
        clothingItem.setActive(true);

        outfit = new Outfit();
        outfit.setId(1L);
        outfit.setUser(user);
        outfit.setLastWornDate(LocalDate.now().minusDays(2));
        outfit.setWearCount(3);
        outfit.setActive(true);
    }

    @Test
    void addItem_ShouldSaveAndReturnItem() {
        when(clothingItemRepository.save(any(ClothingItem.class))).thenReturn(clothingItem);

        ClothingItem result = wardrobeService.addItem(clothingItem);

        assertEquals(clothingItem, result);
        verify(clothingItemRepository, times(1)).save(clothingItem);
    }

    @Test
    void getUserItems_WithPageable_ShouldReturnPagedItems() {
        Page<ClothingItem> page = new PageImpl<>(Arrays.asList(clothingItem));
        when(clothingItemRepository.findByUserIdAndIsActiveTrue(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<ClothingItem> result = wardrobeService.getUserItems(1L, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(clothingItem, result.getContent().get(0));
        verify(clothingItemRepository, times(1)).findByUserIdAndIsActiveTrue(eq(1L), any(Pageable.class));
    }

    @Test
    void getUserItems_WithoutPageable_ShouldReturnAllItems() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getUserItems(1L);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findByUserIdAndIsActiveTrue(1L);
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(clothingItem));

        Optional<ClothingItem> result = wardrobeService.getItemById(1L);

        assertTrue(result.isPresent());
        assertEquals(clothingItem, result.get());
        verify(clothingItemRepository, times(1)).findById(1L);
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem() {
        when(clothingItemRepository.save(any(ClothingItem.class))).thenReturn(clothingItem);

        ClothingItem result = wardrobeService.updateItem(clothingItem);

        assertEquals(clothingItem, result);
        verify(clothingItemRepository, times(1)).save(clothingItem);
    }

    @Test
    void deleteItem_WhenItemExists_ShouldSetActiveFalse() {
        when(clothingItemRepository.findById(1L)).thenReturn(Optional.of(clothingItem));
        when(clothingItemRepository.save(any(ClothingItem.class))).thenReturn(clothingItem);

        wardrobeService.deleteItem(1L);

        assertFalse(clothingItem.isActive());
        verify(clothingItemRepository, times(1)).findById(1L);
        verify(clothingItemRepository, times(1)).save(clothingItem);
    }

    @Test
    void deleteItem_WhenItemDoesNotExist_ShouldThrowException() {
        when(clothingItemRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> wardrobeService.deleteItem(1L));

        assertEquals("Item not found", exception.getMessage());
        verify(clothingItemRepository, times(1)).findById(1L);
        verify(clothingItemRepository, never()).save(any());
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.searchByUserAndTerm(user, "T-Shirt")).thenReturn(items);

        List<ClothingItem> result = wardrobeService.searchItems(user, "T-Shirt");

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).searchByUserAndTerm(user, "T-Shirt");
    }

    @Test
    void getItemsByCategory_ShouldReturnItemsByCategory() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        Category category = clothingItem.getCategory();
        when(clothingItemRepository.findByUserAndCategory(user, category)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getItemsByCategory(user, category);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findByUserAndCategory(user, category);
    }

    @Test
    void getItemsByColor_ShouldReturnItemsByColor() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByUserAndColor(user, Color.BLUE)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getItemsByColor(user, Color.BLUE);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findByUserAndColor(user, Color.BLUE);
    }

    @Test
    void getItemsBySeason_ShouldReturnItemsBySeason() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByUserAndSeason(user, Season.SUMMER)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getItemsBySeason(user, Season.SUMMER);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findByUserAndSeason(user, Season.SUMMER);
    }

    @Test
    void getItemsByOccasion_ShouldReturnItemsByOccasion() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByUserAndOccasion(user, Occasion.CASUAL)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getItemsByOccasion(user, Occasion.CASUAL);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findByUserAndOccasion(user, Occasion.CASUAL);
    }

    @Test
    void getUnusedItems_ShouldReturnUnusedItems() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        LocalDate since = LocalDate.now().minusDays(30);
        when(clothingItemRepository.findUnusedItems(user.getId(), since)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getUnusedItems(user.getId(), since);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findUnusedItems(user.getId(), since);
    }

    @Test
    void getNeverUsedItems_ShouldReturnNeverUsedItems() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findNeverUsedItems(user)).thenReturn(items);

        List<ClothingItem> result = wardrobeService.getNeverUsedItems(user);

        assertEquals(1, result.size());
        assertEquals(clothingItem, result.get(0));
        verify(clothingItemRepository, times(1)).findNeverUsedItems(user);
    }

    @Test
    void markItemAsUsed_ShouldUpdateUsageDetails() {
        when(clothingItemRepository.save(any(ClothingItem.class))).thenReturn(clothingItem);

        wardrobeService.markItemAsUsed(clothingItem);

        assertEquals(LocalDate.now(), clothingItem.getLastUsedDate());
        assertEquals(6, clothingItem.getUsageCount());
        verify(clothingItemRepository, times(1)).save(clothingItem);
    }

    @Test
    void getActiveItemsCount_ShouldReturnCount() {
        when(clothingItemRepository.countByIsActiveTrue()).thenReturn(10L);

        long result = wardrobeService.getActiveItemsCount();

        assertEquals(10L, result);
        verify(clothingItemRepository, times(1)).countByIsActiveTrue();
    }

    @Test
    void getPeakUsagePeriods_ShouldReturnUsagePeriods() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        List<Outfit> outfits = Arrays.asList(outfit);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        when(clothingItemRepository.findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate)).thenReturn(items);
        when(outfitRepository.findByLastWornDateBetweenAndIsActiveTrue(startDate, endDate)).thenReturn(outfits);

        List<UsagePeriodDto> result = wardrobeService.getPeakUsagePeriods(startDate, endDate);

        assertFalse(result.isEmpty());
        assertEquals(8, result.size()); // 7 days + 1 for inclusive end date
        verify(clothingItemRepository, times(1)).findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate);
        verify(outfitRepository, times(1)).findByLastWornDateBetweenAndIsActiveTrue(startDate, endDate);
    }

    @Test
    void getItemUsageTrends_ShouldReturnTrendsAndNewItems() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<ClothingItem> usedItems = Arrays.asList(clothingItem);
        List<ClothingItem> newItems = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate)).thenReturn(usedItems);
        when(clothingItemRepository.findByCreatedAtBetweenAndIsActiveTrue(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(newItems);

        ItemUsageTrendDto result = wardrobeService.getItemUsageTrends();

        assertEquals(8, result.getUsageTrends().size()); // 7 days + 1 for inclusive end date
        assertEquals(1, result.getNewlyAddedItems().size());
        assertEquals("T-Shirt", result.getNewlyAddedItems().get(0).getName());
        verify(clothingItemRepository, times(1)).findByLastUsedDateBetweenAndIsActiveTrue(startDate, endDate);
        verify(clothingItemRepository, times(1)).findByCreatedAtBetweenAndIsActiveTrue(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
package com.peti.service;

import com.peti.constants.Season;
import com.peti.dto.CategoryUsageDto;
import com.peti.model.Category;
import com.peti.model.ClothingItem;
import com.peti.repository.CategoryRepository;
import com.peti.repository.ClothingItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ClothingItemRepository clothingItemRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private ClothingItem clothingItem;

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Shirts", "Casual shirts", true);
        clothingItem = new ClothingItem();
        clothingItem.setId(1L);
        clothingItem.setCategory(category);
        clothingItem.setUsageCount(5);
        clothingItem.setLastUsedDate(LocalDate.now().minusDays(10));
        clothingItem.setActive(true);
    }

    @Test
    void findAll_ShouldReturnActiveCategories() {
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(categories);

        List<Category> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals(category, result.get(0));
        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void findById_WhenCategoryExists_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(category, result.get());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.findById(1L);

        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void save_ShouldSaveAndReturnCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.save(category);

        assertEquals(category, result);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void deleteById_WhenCategoryExists_ShouldSetActiveFalse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.deleteById(1L);

        assertFalse(category.isActive());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void deleteById_WhenCategoryDoesNotExist_ShouldThrowException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> categoryService.deleteById(1L));

        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getTopUsedCategories_ShouldReturnTopCategories() {
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByLastUsedDateAfterAndIsActiveTrue(any(LocalDate.class))).thenReturn(items);

        List<CategoryUsageDto> result = categoryService.getTopUsedCategories(5);

        assertEquals(1, result.size());
        assertEquals("Shirts", result.get(0).getCategoryName());
        assertEquals(5, result.get(0).getUsageCount());
        verify(clothingItemRepository, times(1)).findByLastUsedDateAfterAndIsActiveTrue(any(LocalDate.class));
    }

    @Test
    void getLeastUsedCategories_ShouldReturnLeastUsedCategories() {
        List<Category> allCategories = Arrays.asList(category);
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(allCategories);
        when(clothingItemRepository.findByLastUsedDateAfterAndIsActiveTrue(any(LocalDate.class))).thenReturn(items);

        List<CategoryUsageDto> result = categoryService.getLeastUsedCategories(5);

        assertEquals(1, result.size());
        assertEquals("Shirts", result.get(0).getCategoryName());
        assertEquals(5, result.get(0).getUsageCount());
        verify(categoryRepository, times(1)).findByIsActiveTrue();
        verify(clothingItemRepository, times(1)).findByLastUsedDateAfterAndIsActiveTrue(any(LocalDate.class));
    }

    @Test
    void getCategoryUsageBySeason_ShouldReturnUsageBySeason() {
        clothingItem.setSeason(Season.SPRING);
        List<ClothingItem> items = Arrays.asList(clothingItem);
        when(clothingItemRepository.findByIsActiveTrue()).thenReturn(items);

        Map<Season, List<CategoryUsageDto>> result = categoryService.getCategoryUsageBySeason();

        assertEquals(Season.values().length, result.size());
        assertEquals(1, result.get(Season.SPRING).size());
        assertEquals("Shirts", result.get(Season.SPRING).get(0).getCategoryName());
        assertEquals(5, result.get(Season.SPRING).get(0).getUsageCount());
        verify(clothingItemRepository, times(1)).findByIsActiveTrue();
    }
}
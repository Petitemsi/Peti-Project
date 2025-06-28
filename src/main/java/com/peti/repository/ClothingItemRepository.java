package com.peti.repository;

import com.peti.model.ClothingItem;
import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {
    
    List<ClothingItem> findByUser(User user);
    
    List<ClothingItem> findByUserAndIsActiveTrue(User user);
    
    List<ClothingItem> findByUserAndCategory(User user, ClothingItem.Category category);
    
    List<ClothingItem> findByUserAndColor(User user, ClothingItem.Color color);
    
    List<ClothingItem> findByUserAndSeason(User user, ClothingItem.Season season);
    
    List<ClothingItem> findByUserAndOccasion(User user, ClothingItem.Occasion occasion);
    
    @Query("SELECT c FROM ClothingItem c WHERE c.user = :user AND c.lastUsedDate < :date OR c.lastUsedDate IS NULL")
    List<ClothingItem> findUnusedItems(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT c FROM ClothingItem c WHERE c.user = :user AND c.usageCount = 0")
    List<ClothingItem> findNeverUsedItems(@Param("user") User user);
    
    @Query("SELECT c FROM ClothingItem c WHERE c.user = :user AND (c.name LIKE %:searchTerm% OR c.description LIKE %:searchTerm%)")
    List<ClothingItem> searchByUserAndTerm(@Param("user") User user, @Param("searchTerm") String searchTerm);
} 
package com.peti.repository;

import com.peti.constants.Color;
import com.peti.constants.Occasion;
import com.peti.constants.Season;
import com.peti.model.Category;
import com.peti.model.ClothingItem;
import com.peti.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

    List<ClothingItem> findByUser(User user);

    @Query("SELECT c FROM ClothingItem c WHERE c.user.id = :userId AND c.isActive = true")
    List<ClothingItem> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);
    @Query("SELECT c FROM ClothingItem c WHERE c.user.id = :userId AND c.isActive = true")
    Page<ClothingItem> findByUserIdAndIsActiveTrue(@Param("userId") Long userId, Pageable pageable);
    List<ClothingItem> findByUserAndCategory(User user, Category category);

    List<ClothingItem> findByUserAndColor(User user, Color color);

    List<ClothingItem> findByUserAndSeason(User user, Season season);

    List<ClothingItem> findByUserAndOccasion(User user, Occasion occasion);

    @Query("SELECT c FROM ClothingItem c WHERE c.user.id = :userId AND c.isActive = true AND (c.lastUsedDate < :date OR c.lastUsedDate IS NULL)")
    List<ClothingItem> findUnusedItems(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT c FROM ClothingItem c WHERE c.user = :user AND c.usageCount = 0")
    List<ClothingItem> findNeverUsedItems(@Param("user") User user);

    @Query("SELECT c FROM ClothingItem c WHERE c.user = :user AND (c.name LIKE %:searchTerm% OR c.description LIKE %:searchTerm%)")
    List<ClothingItem> searchByUserAndTerm(@Param("user") User user, @Param("searchTerm") String searchTerm);

    long countByIsActiveTrue();

    List<ClothingItem> findByLastUsedDateAfterAndIsActiveTrue(LocalDate thirtyDaysAgo);

    List<ClothingItem> findByIsActiveTrue();

    List<ClothingItem> findByLastUsedDateBetweenAndIsActiveTrue(LocalDate startDate, LocalDate endDate);

    List<ClothingItem> findByCreatedAtBetweenAndIsActiveTrue(LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
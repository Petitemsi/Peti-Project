package com.peti.repository;

import com.peti.model.UsageLog;
import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    
    List<UsageLog> findByUser(User user);
    
    List<UsageLog> findByUserOrderByUsageDateDesc(User user);
    
    @Query("SELECT u FROM UsageLog u WHERE u.user = :user AND u.usageDate >= :startDate AND u.usageDate <= :endDate")
    List<UsageLog> findByUserAndDateRange(@Param("user") User user, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT u FROM UsageLog u WHERE u.clothingItem.id = :itemId ORDER BY u.usageDate DESC")
    List<UsageLog> findByClothingItemId(@Param("itemId") Long itemId);
} 
package com.peti.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothing_item_id", nullable = false)
    private ClothingItem clothingItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id")
    private Outfit outfit;
    
    private LocalDate usageDate;
    
    private LocalDateTime createdAt;
    
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usageDate == null) {
            usageDate = LocalDate.now();
        }
    }
} 
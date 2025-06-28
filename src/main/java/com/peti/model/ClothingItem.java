package com.peti.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clothing_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClothingItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    private Color color;
    
    @Enumerated(EnumType.STRING)
    private Season season;
    
    @Enumerated(EnumType.STRING)
    private Occasion occasion;
    
    private String imageUrl;
    
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private LocalDate lastUsedDate;
    
    private int usageCount = 0;
    
    private LocalDateTime createdAt;
    
    private boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum Category {
        SHIRT, PANTS, DRESS, SKIRT, JACKET, COAT, SHOES, BAG, ACCESSORY, UNDERWEAR, SOCKS
    }
    
    public enum Color {
        BLACK, WHITE, RED, BLUE, GREEN, YELLOW, PURPLE, PINK, ORANGE, BROWN, GRAY, NAVY, BEIGE, MULTI
    }
    
    public enum Season {
        SPRING, SUMMER, FALL, WINTER, ALL_SEASON
    }
    
    public enum Occasion {
        CASUAL, BUSINESS, FORMAL, SPORT, PARTY, BEACH, HIKING, SLEEP
    }
} 
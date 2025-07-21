package com.peti.model;

import com.peti.constants.Color;
import com.peti.constants.Occasion;
import com.peti.constants.Season;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

   @ManyToOne
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
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;
    
    private LocalDate lastUsedDate;
    
    private int usageCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private boolean isActive = true;

} 
package com.peti.model;

import com.peti.constants.Occasion;
import com.peti.constants.Season;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "outfits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Outfit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Occasion occasion;

    @Enumerated(EnumType.STRING)
    private Season season;

    @ManyToMany
    @JoinTable(
            name = "outfit_clothes",
            joinColumns = @JoinColumn(name = "outfit_id"),
            inverseJoinColumns = @JoinColumn(name = "clothes_id")
    )
    private Set<ClothingItem> clothes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate lastWornDate;

    private int wearCount = 0;

    private LocalDateTime createdAt;

    private boolean isActive = true;

    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
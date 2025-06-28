package com.peti.repository;

import com.peti.model.Outfit;
import com.peti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    
    List<Outfit> findByUser(User user);
    
    List<Outfit> findByUserAndIsActiveTrue(User user);
    
    List<Outfit> findByUserAndOccasion(User user, Outfit.Occasion occasion);
    
    List<Outfit> findByUserAndSeason(User user, Outfit.Season season);
} 
package com.peti.service;

import com.peti.dto.ClothingItemDto;
import com.peti.dto.OutfitDto;
import com.peti.dto.OutfitResponseDto;
import com.peti.engine.PetiEngine;
import com.peti.model.*;
import com.peti.repository.ClothingItemRepository;
import com.peti.repository.OutfitRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutfitService {

    private static final Logger logger = LoggerFactory.getLogger(OutfitService.class);

    @Autowired
    private OutfitRepository outfitRepository;
    @Autowired
    private PetiEngine petiEngine;
    @Autowired
    private ClothingItemRepository clothingItemRepository;

    @Transactional
    public Outfit createOutfit(OutfitDto outfit) {
        logger.info("Creating outfit with name: {}", outfit.getName());
        User user = petiEngine.getLoggedInUser();
        if (user == null) {
            logger.error("No logged-in user found");
            throw new RuntimeException("User must be logged in to create an outfit");
        }

        // Validate items list
        if (outfit.getItems() == null || outfit.getItems().isEmpty()) {
            logger.warn("No items provided for outfit: {}", outfit.getName());
            throw new IllegalArgumentException("Outfit must have at least one item");
        }

        // Create new Outfit instance
        Outfit newOutfit = new Outfit();
        newOutfit.setUser(user);
        newOutfit.setName(outfit.getName());
        newOutfit.setOccasion(outfit.getOccasion());
        newOutfit.setSeason(outfit.getSeason());
        newOutfit.setDescription(outfit.getDescription());
        newOutfit.setActive(true);
        Set<ClothingItem> clothes = new HashSet<>(clothingItemRepository.findAllById(outfit.getItems()));


        // Set the items to the outfit
        newOutfit.setClothes(clothes);

        // Save the outfit (cascading will save the items)
        Outfit savedOutfit = outfitRepository.save(newOutfit);
        logger.info("Successfully saved outfit with ID: {}", savedOutfit.getId());

        return savedOutfit;
    }

    @Transactional(readOnly = true)
    public OutfitResponseDto getActiveOutfitById(Long id) {
        logger.debug("Retrieving active outfit with ID: {}", id);
        Outfit outfit = outfitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Active outfit not found with ID: " + id));
        Hibernate.initialize(outfit.getClothes()); // Initialize clothes
        outfit.getClothes().forEach(clothingItem -> Hibernate.initialize(clothingItem.getUser())); // Initialize user
        return OutfitResponseDto.fromEntity(outfit);
    }

    @Transactional(readOnly = true)
    public List<OutfitResponseDto> getAllActiveOutfits() {
        User user = petiEngine.getLoggedInUser();
        return outfitRepository.findByUserAndIsActiveTrue(user).stream()
                .peek(outfit -> {
                    Hibernate.initialize(outfit.getClothes()); // Initialize clothes
                    outfit.getClothes().forEach(clothingItem -> Hibernate.initialize(clothingItem.getUser())); // Initialize user
                })
                .map(OutfitResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OutfitResponseDto updateOutfit(Long id, OutfitDto request) {
        logger.info("Updating outfit with ID: {}", id);
        User user = petiEngine.getLoggedInUser();
        if (user == null) {
            logger.error("No logged-in user found");
            throw new RuntimeException("User must be logged in to update an outfit");
        }

        Outfit outfit = outfitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {
                    logger.error("Active outfit not found with ID: {}", id);
                    return new EntityNotFoundException("Active outfit not found with id: " + id);
                });

        if (request.getItems() != null && request.getItems().isEmpty()) {
            logger.warn("Empty clothesIds list provided for outfit: {}", request.getName());
            throw new IllegalArgumentException("Outfit must have at least one clothing item if clothesIds are provided");
        }

        if (request.getName() != null) {
            outfit.setName(request.getName());
        }
        if (request.getOccasion() != null) {
            outfit.setOccasion(request.getOccasion());
        }
        if (request.getSeason() != null) {
            outfit.setSeason(request.getSeason());
        }
        if (request.getDescription() != null) {
            outfit.setDescription(request.getDescription());
        }

        if (request.getItems() != null) {
            Set<ClothingItem> updatedClothes = new HashSet<>(clothingItemRepository.findAllById(request.getItems()));
            if (updatedClothes.size() != request.getItems().size()) {
                logger.warn("Some clothing items not found for outfit ID: {}", id);
                throw new IllegalArgumentException("One or more clothing items not found");
            }
            outfit.setClothes(updatedClothes);
        }

        Outfit updatedOutfit = outfitRepository.save(outfit);
        Hibernate.initialize(updatedOutfit.getClothes());
        updatedOutfit.getClothes().forEach(clothingItem -> Hibernate.initialize(clothingItem.getUser()));
        logger.info("Successfully updated outfit with ID: {}", id);
        return OutfitResponseDto.fromEntity(updatedOutfit);
    }

    @Transactional
    public void softDeleteOutfit(Long id) {
        logger.info("Soft deleting outfit with ID: {}", id);
        Outfit outfit = outfitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {
                    logger.error("Active outfit not found with ID: {}", id);
                    return new RuntimeException("Active outfit not found with id: " + id);
                });
        outfit.setActive(false);
        outfitRepository.save(outfit);
        logger.info("Successfully soft deleted outfit with ID: {}", id);
    }

    @Transactional
    public Outfit markAsWorn(Long id) {
        logger.info("Marking outfit as worn with ID: {}", id);
        Outfit outfit = outfitRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> {
                    logger.error("Active outfit not found with ID: {}", id);
                    return new RuntimeException("Active outfit not found with id: " + id);
                });

        // Update outfit details
        outfit.setLastWornDate(LocalDate.now());
        outfit.setWearCount(outfit.getWearCount() + 1);

        // Initialize clothes to avoid lazy loading issues
        Hibernate.initialize(outfit.getClothes());

        // Update each clothing item's usage count and last used date
        Set<ClothingItem> clothingItems = outfit.getClothes();
        if (clothingItems == null || clothingItems.isEmpty()) {
            logger.warn("No clothing items found for outfit ID: {}", id);
            throw new IllegalStateException("Outfit has no associated clothing items");
        }

        for (ClothingItem item : clothingItems) {
            item.setUsageCount(item.getUsageCount() + 1);
            item.setLastUsedDate(LocalDate.now());
            clothingItemRepository.save(item);
            logger.debug("Updated clothing item ID: {} with new usage count: {} and last used date: {}",
                    item.getId(), item.getUsageCount(), item.getLastUsedDate());
        }

        // Save the updated outfit
        Outfit updatedOutfit = outfitRepository.save(outfit);
        logger.info("Successfully marked outfit as worn with ID: {}", id);
        return updatedOutfit;
    }
    @Transactional(readOnly = true)
    public Set<ClothingItemDto> getClothingItemsByOutfitId(Long outfitId) {
        logger.debug("Retrieving clothing items for outfit with ID: {}", outfitId);
        Outfit outfit = outfitRepository.findByIdAndIsActiveTrue(outfitId)
                .orElseThrow(() -> {
                    logger.error("Active outfit not found with ID: {}", outfitId);
                    return new RuntimeException("Active outfit not found with id: " + outfitId);
                });

        Hibernate.initialize(outfit.getClothes()); // Ensure clothes are initialized
        return outfit.getClothes().stream()
                .map(ClothingItemDto::fromEntity)
                .collect(Collectors.toSet());
    }

    public long getOutfitUsageCountLast7Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        return outfitRepository.countByLastWornDateBetweenAndIsActiveTrue(startDate, endDate);
    }
}
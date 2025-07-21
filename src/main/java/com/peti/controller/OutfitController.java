package com.peti.controller;

import com.peti.dto.ClothingItemDto;
import com.peti.dto.OutfitDto;
import com.peti.dto.OutfitResponseDto;
import com.peti.engine.PetiEngine;
import com.peti.model.*;
import com.peti.service.OutfitService;
//import com.peti.service.OutfitSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/outfits")
public class OutfitController {

    @Autowired
    private OutfitService outfitService;
//    @Autowired
//    private OutfitSuggestionService outfitSuggestionService;
    @Autowired
    private PetiEngine petiEngine;

    @PostMapping
    public ResponseEntity<Outfit> createOutfit(@RequestBody OutfitDto outfit) {
        return ResponseEntity.ok(outfitService.createOutfit(outfit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutfitResponseDto> getOutfitById(@PathVariable Long id) {
        return ResponseEntity.ok(outfitService.getActiveOutfitById(id));
    }

    @GetMapping("/clothingItems/{outfitId}")
    public ResponseEntity<Set<ClothingItemDto>> getClothingItemsByOutfitId(@PathVariable Long outfitId) {
        Set<ClothingItemDto> clothingItems = outfitService.getClothingItemsByOutfitId(outfitId);
        return ResponseEntity.ok(clothingItems);
    }

    @GetMapping
    public ResponseEntity<List<OutfitResponseDto>> getAllOutfits() {
        return ResponseEntity.ok(outfitService.getAllActiveOutfits());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OutfitResponseDto> updateOutfit(@PathVariable Long id, @RequestBody OutfitDto request) {
        return ResponseEntity.ok(outfitService.updateOutfit(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutfit(@PathVariable Long id) {
        outfitService.softDeleteOutfit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/wear")
    public ResponseEntity<Outfit> markAsWorn(@PathVariable Long id) {
        return ResponseEntity.ok(outfitService.markAsWorn(id));
    }

//    @PostMapping("/suggest")
//    public ResponseEntity<List<OutfitResponseDto>> generateOutfitSuggestions(@RequestBody OutfitSuggestionRequestDto request) {
//        return ResponseEntity.ok("ok");
//    }
    @GetMapping("/usage-count-last-7-days")
    public ResponseEntity<Long> getOutfitUsageCountLast7Days() {
        long count = outfitService.getOutfitUsageCountLast7Days();
        return ResponseEntity.ok(count);
    }
}
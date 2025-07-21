package com.peti.controller;

import com.peti.dto.ClothingItemDto;
import com.peti.model.ClothingItem;
import com.peti.model.ErrorResponse;
import com.peti.model.OutfitSuggestionRequestDto;
import com.peti.model.User;
import com.peti.service.OutfitSuggestionService;
import com.peti.service.WardrobeService;
import com.peti.engine.PetiEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/wardrobe")
public class OutfitSuggestionController {

    private final OutfitSuggestionService outfitSuggestionService;
    private final WardrobeService wardrobeService;
    private final PetiEngine petiEngine;

    public OutfitSuggestionController(OutfitSuggestionService outfitSuggestionService, WardrobeService wardrobeService, PetiEngine petiEngine) {
        this.outfitSuggestionService = outfitSuggestionService;
        this.wardrobeService = wardrobeService;
        this.petiEngine = petiEngine;
    }


    @PostMapping("/suggest-outfit")
    @ResponseBody
    ResponseEntity<?> suggestOutfit(@RequestBody OutfitSuggestionRequestDto request) {

        try {
            User user = petiEngine.getLoggedInUser();
            List<ClothingItem> clothingItems = wardrobeService.getUserItems(user.getId());
            if (clothingItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No active clothing items found for the user"));
            }
            String content = outfitSuggestionService.suggestOutfits(request, clothingItems);
            if (content == null || content.trim().isEmpty() || content.equalsIgnoreCase("none")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No outfit suitable for the occasion and weather"));
            }

            // Extract comma-separated ID list from the first line
            String idList = extractIdList(content);
            if (idList == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Invalid response from AI: No valid ID list found"));
            }

            List<Long> selectedItemIds;
            try {
                selectedItemIds = Arrays.stream(idList.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Invalid response from AI: Unable to parse IDs"));
            }

            List<ClothingItem> recommendedItems = clothingItems.stream()
                    .filter(item -> selectedItemIds.contains(item.getId()))
                    .collect(Collectors.toList());

            if (recommendedItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("No matching clothing items found"));
            }

            List<ClothingItemDto> recommendedItemDtos = recommendedItems.stream()
                    .map(ClothingItemDto::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(recommendedItemDtos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error generating outfit suggestion: " + e.getMessage()));
        }
    }

    private String extractIdList(String content) {
        // Pattern to match a sequence of comma-separated numbers, optionally surrounded by quotes
        Pattern pattern = Pattern.compile("\"?\\b\\d+(?:,\\s*\\d+)*\\b\"?");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            // Get the matched group and remove any surrounding quotes
            String idList = matcher.group().replaceAll("^\"|\"$", "");
            // Verify the extracted string is a valid comma-separated number list
            if (idList.matches("\\d+(,\\s*\\d+)*")) {
                return idList;
            }
        }
        return null;
    }
}
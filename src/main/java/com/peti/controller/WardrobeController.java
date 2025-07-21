package com.peti.controller;

import com.peti.dto.ClothingItemDto;
import com.peti.dto.ItemUsageTrendDto;
import com.peti.dto.UsagePeriodDto;
import com.peti.engine.PetiEngine;
import com.peti.model.Category;
import com.peti.model.ClothingItem;
import com.peti.model.ErrorResponse;
import com.peti.model.User;
import com.peti.service.CloudinaryService;
import com.peti.service.UserService;
import com.peti.service.WardrobeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/wardrobe")
@RequiredArgsConstructor
public class WardrobeController {

    private final WardrobeService wardrobeService;
    private final UserService userService;
    @Autowired
    PetiEngine petiEngine;
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/items")
    @ResponseBody
    public ResponseEntity<Page<ClothingItem>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = petiEngine.getLoggedInUser();
        Page<ClothingItem> items = wardrobeService.getUserItems(
                user.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/search")
    @ResponseBody
    public ResponseEntity<List<ClothingItem>> searchItemsByQuery(@RequestParam String q) {
        User user = petiEngine.getLoggedInUser();
        List<ClothingItem> items = wardrobeService.searchItems(user, q);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/category/{category}")
    @ResponseBody
    public ResponseEntity<List<ClothingItem>> getItemsByCategoryApi(@PathVariable Category category) {
        User user = petiEngine.getLoggedInUser();
        List<ClothingItem> items = wardrobeService.getItemsByCategory(user, category);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/items/{id}/use")
    @ResponseBody
    public ResponseEntity<String> markItemAsUsedApi(@PathVariable Long id) {
        User user = petiEngine.getLoggedInUser();
        ClothingItem item = wardrobeService.getItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }

        wardrobeService.markItemAsUsed(item);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/items/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteItemById(@PathVariable Long id) {
        User user = petiEngine.getLoggedInUser();
        ClothingItem item = wardrobeService.getItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }

        wardrobeService.deleteItem(id);
        return ResponseEntity.ok("success");
    }
    @GetMapping("/items/{id}")
    @ResponseBody
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            User user = petiEngine.getLoggedInUser();
            ClothingItem item = wardrobeService.getItemById(id)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching item: " + e.getMessage()));
        }
    }
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> addItem(
            @ModelAttribute ClothingItem clothingItem,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        User user = petiEngine.getLoggedInUser();
        clothingItem.setUser(user);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            clothingItem.setImageUrl(imageUrl);
        }

        wardrobeService.addItem(clothingItem);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ErrorResponse("Item added successfully."));
    }
    @GetMapping("/search")
    public String searchItems(@RequestParam String q, Authentication authentication, Model model) {
        User user = petiEngine.getLoggedInUser();
        List<ClothingItem> items = wardrobeService.searchItems(user, q);
        model.addAttribute("items", items);
        model.addAttribute("searchTerm", q);
        return "dashboard";
    }

    @PostMapping("/{id}/use")
    @ResponseBody
    public String markItemAsUsed(@PathVariable Long id) {
        User user = petiEngine.getLoggedInUser();
        ClothingItem item = wardrobeService.getItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        wardrobeService.markItemAsUsed(item);
        return "success";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteItem(@PathVariable Long id) {
        User user = petiEngine.getLoggedInUser();
        ClothingItem item = wardrobeService.getItemById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        wardrobeService.deleteItem(id);
        return "success";
    }

    @PutMapping("/items/{id}")
    @ResponseBody
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @ModelAttribute ClothingItem clothingItem,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        try {
            User user = petiEngine.getLoggedInUser();
            ClothingItem existingItem = wardrobeService.getItemById(id)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!existingItem.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }

            existingItem.setName(clothingItem.getName());
            existingItem.setCategory(clothingItem.getCategory());
            existingItem.setColor(clothingItem.getColor());
            existingItem.setOccasion(clothingItem.getOccasion());
            existingItem.setSeason(clothingItem.getSeason());
            existingItem.setDescription(clothingItem.getDescription());

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                existingItem.setImageUrl(imageUrl);
            }

            ClothingItem updatedItem = wardrobeService.updateItem(existingItem);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error updating item: " + e.getMessage()));
        }
    }

    @GetMapping("/items/count")
    public ResponseEntity<Long> getActiveItemsCount() {
        long count = wardrobeService.getActiveItemsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/peak-usage")
    public ResponseEntity<List<UsagePeriodDto>> getPeakUsagePeriods() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        List<UsagePeriodDto> usagePeriods = wardrobeService.getPeakUsagePeriods(startDate, endDate);
        return ResponseEntity.ok(usagePeriods);
    }

    @GetMapping("/item-usage-trends")
    public ResponseEntity<ItemUsageTrendDto> getItemUsageTrends() {
        ItemUsageTrendDto trends = wardrobeService.getItemUsageTrends();
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/items/unused/last-30-days")
    @ResponseBody
    public ResponseEntity<List<ClothingItemDto>> getUnusedItemsLast30Days() {
        User user = petiEngine.getLoggedInUser();
        LocalDate since = LocalDate.now().minusDays(30);
        List<ClothingItem> unusedItems = wardrobeService.getUnusedItems(user.getId(), since);
        List<ClothingItemDto> unusedItemDtos = unusedItems.stream()
                .map(ClothingItemDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(unusedItemDtos);
    }

    @GetMapping("/items/total")
    @ResponseBody
    public ResponseEntity<Long> getTotalItemsInWardrobe() {
        User user = petiEngine.getLoggedInUser();
        long totalItems = wardrobeService.getUserItems(user.getId()).size();
        return ResponseEntity.ok(totalItems);
    }

    @GetMapping("/items/unused/total")
    @ResponseBody
    public ResponseEntity<Long> getTotalUnusedItemsInWardrobe() {
        User user = petiEngine.getLoggedInUser();
        LocalDate since = LocalDate.now().minusDays(30);
        long totalUnusedItems = wardrobeService.getUnusedItems(user.getId(), since).size();
        return ResponseEntity.ok(totalUnusedItems);
    }

    @GetMapping("/items/unused/percentage")
    @ResponseBody
    public ResponseEntity<Double> getUnusedItemsPercentage() {
        User user = petiEngine.getLoggedInUser();
        LocalDate since = LocalDate.now().minusDays(30);
        long totalItems = wardrobeService.getUserItems(user.getId()).size();
        long unusedItems = wardrobeService.getUnusedItems(user.getId(), since).size();
        double percentage = totalItems > 0 ? (double) unusedItems / totalItems * 100 : 0.0;
        return ResponseEntity.ok(percentage);
    }
}
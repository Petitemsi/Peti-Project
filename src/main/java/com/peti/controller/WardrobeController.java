package com.peti.controller;

import com.peti.model.ClothingItem;
import com.peti.model.User;
import com.peti.service.UserService;
import com.peti.service.WardrobeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/wardrobe")
@RequiredArgsConstructor
public class WardrobeController {
    
    private final WardrobeService wardrobeService;
    private final UserService userService;
    
    @GetMapping
    public String dashboard(Model model) {
        // Return the dashboard template without server-side data
        // Data will be loaded via JavaScript API calls
        return "dashboard";
    }
    
    // REST API endpoints for JavaScript
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<List<ClothingItem>> getItems(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ClothingItem> items = wardrobeService.getUserItems(user);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/api/items/search")
    @ResponseBody
    public ResponseEntity<List<ClothingItem>> searchItemsApi(@RequestParam String q, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ClothingItem> items = wardrobeService.searchItems(user, q);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/api/items/category/{category}")
    @ResponseBody
    public ResponseEntity<List<ClothingItem>> getItemsByCategoryApi(@PathVariable ClothingItem.Category category,
                                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ClothingItem> items = wardrobeService.getItemsByCategory(user, category);
        return ResponseEntity.ok(items);
    }
    
    @PostMapping("/api/items/{id}/use")
    @ResponseBody
    public ResponseEntity<String> markItemAsUsedApi(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ClothingItem item = wardrobeService.getItemById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        
        wardrobeService.markItemAsUsed(item);
        return ResponseEntity.ok("success");
    }
    
    @DeleteMapping("/api/items/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteItemApi(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ClothingItem item = wardrobeService.getItemById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("Unauthorized");
        }
        
        wardrobeService.deleteItem(id);
        return ResponseEntity.ok("success");
    }
    
    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }
    
    @PostMapping("/upload")
    public String uploadItem(@RequestParam String name,
                            @RequestParam ClothingItem.Category category,
                            @RequestParam(required = false) ClothingItem.Color color,
                            @RequestParam(required = false) ClothingItem.Season season,
                            @RequestParam(required = false) ClothingItem.Occasion occasion,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) MultipartFile image,
                            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        ClothingItem item = new ClothingItem();
        item.setName(name);
        item.setCategory(category);
        item.setColor(color);
        item.setSeason(season);
        item.setOccasion(occasion);
        item.setDescription(description);
        item.setUser(user);
        
        // TODO: Handle image upload to Cloudinary or local storage
        if (image != null && !image.isEmpty()) {
            // item.setImageUrl(uploadImage(image));
        }
        
        wardrobeService.addItem(item);
        return "redirect:/wardrobe";
    }
    
    @GetMapping("/search")
    public String searchItems(@RequestParam String q, Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        List<ClothingItem> items = wardrobeService.searchItems(user, q);
        model.addAttribute("items", items);
        model.addAttribute("searchTerm", q);
        return "dashboard";
    }
    
    @GetMapping("/category/{category}")
    public String getItemsByCategory(@PathVariable ClothingItem.Category category,
                                   Authentication authentication,
                                   Model model) {
        User user = (User) authentication.getPrincipal();
        List<ClothingItem> items = wardrobeService.getItemsByCategory(user, category);
        model.addAttribute("items", items);
        model.addAttribute("category", category);
        return "dashboard";
    }
    
    @PostMapping("/{id}/use")
    @ResponseBody
    public String markItemAsUsed(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
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
    public String deleteItem(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ClothingItem item = wardrobeService.getItemById(id)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        
        wardrobeService.deleteItem(id);
        return "success";
    }
} 
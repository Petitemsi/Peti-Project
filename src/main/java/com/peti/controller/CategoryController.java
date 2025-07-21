package com.peti.controller;

import com.peti.constants.Season;
import com.peti.dto.CategoryUsageDto;
import com.peti.model.Category;
import com.peti.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.findById(id)
                .map(existingCategory -> {
                    existingCategory.setName(category.getName());
                    existingCategory.setDescription(category.getDescription());
                    return ResponseEntity.ok(categoryService.save(existingCategory));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryService.findById(id).isPresent()) {
            categoryService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/top-used")
    public ResponseEntity<List<CategoryUsageDto>> getTopUsedCategories() {
        return ResponseEntity.ok(categoryService.getTopUsedCategories(5));
    }

    @GetMapping("/least-used")
    public ResponseEntity<List<CategoryUsageDto>> getLeastUsedCategories() {
        return ResponseEntity.ok(categoryService.getLeastUsedCategories(5));
    }

    @GetMapping("/seasonal-usage")
    public ResponseEntity<Map<Season, List<CategoryUsageDto>>> getCategoryUsageBySeason() {
        return ResponseEntity.ok(categoryService.getCategoryUsageBySeason());
    }
}
package com.peti.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// DTO class for category usage
public class CategoryUsageDto {
    private String categoryName;
    private long usageCount;
}
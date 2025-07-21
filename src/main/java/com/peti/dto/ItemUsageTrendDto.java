package com.peti.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUsageTrendDto {
    private List<UsagePeriodDto> usageTrends;
    private List<ClothingItemDto> newlyAddedItems;
}
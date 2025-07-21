package com.peti.model;

import com.peti.constants.Occasion;
import com.peti.constants.Season;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutfitSuggestionRequestDto {
    private Occasion occasion;
    private Season season;
    private WeatherDetails weatherDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherDetails {
        private Double temperature; // Nullable to handle optional field
        private String weatherCondition; // Nullable to handle optional field
    }
}
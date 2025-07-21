package com.peti.dto;

import com.peti.constants.Occasion;
import com.peti.constants.Season;
import com.peti.model.Outfit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for {@link Outfit}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutfitDto {
    private Long id;
    private String name;
    private Season season;
    private Occasion occasion;
    private List<Long> items;
    private String description;
}
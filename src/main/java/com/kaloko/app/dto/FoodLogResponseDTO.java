package com.kaloko.app.dto;

import java.time.LocalDate;
import java.util.Map;

public record FoodLogResponseDTO(
        Long id,
        Long foodId,
        String foodName,
        Double grams,
        LocalDate date,
        String mealName,
        Integer calories,
        Integer protein,
        Integer carbs,
        Integer fats,
        Map<String, String> micronutrients
) {}

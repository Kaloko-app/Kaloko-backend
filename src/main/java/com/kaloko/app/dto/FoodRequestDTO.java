package com.kaloko.app.dto;

import java.util.Map;

public record FoodRequestDTO(
        String name,
        String barcode,
        Integer calories,
        Integer protein,
        Integer carbs,
        Integer fats,
        Map<String, String> micronutrients,
        Boolean isPublic
) {}

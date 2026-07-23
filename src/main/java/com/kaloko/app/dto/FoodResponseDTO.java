package com.kaloko.app.dto;

import java.util.Map;

public record FoodResponseDTO(
        Long id,
        String name,
        String barcode,
        Integer calories,
        Integer protein,
        Integer carbs,
        Integer fats,
        Map<String, String> micronutrients,
        Boolean isPublic,
        Long createdById,
        Double servingSize,
        String servingUnit
) {}

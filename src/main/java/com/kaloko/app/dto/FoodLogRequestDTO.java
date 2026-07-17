package com.kaloko.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record FoodLogRequestDTO(
        @NotNull(message = "Food ID is required")
        Long foodId,

        @NotNull(message = "Grams are required")
        @Positive(message = "Grams must be a positive number")
        Double grams,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Meal name is required")
        String mealName
) {}

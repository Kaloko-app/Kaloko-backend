package com.kaloko.app.dto;

import com.kaloko.app.entity.ActivityLevel;
import com.kaloko.app.entity.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UserMetricsUpdateRequestDTO(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Current weight is required")
        @Positive(message = "Current weight must be a positive number")
        Float currentWeight,

        @NotNull(message = "Weight goal is required")
        @Positive(message = "Weight goal must be a positive number")
        Float weightGoal,

        @NotNull(message = "Height is required")
        @Positive(message = "Height must be a positive number")
        Float height,

        @NotNull(message = "Age is required")
        @Positive(message = "Age must be a positive number")
        Integer age,

        @NotNull(message = "Activity level is required")
        ActivityLevel activityLevel,

        @NotNull(message = "Gender is required")
        Gender gender,

        @Positive(message = "Body fat percentage must be a positive number")
        Float bodyFatPercentage
) {}

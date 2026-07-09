package com.kaloko.app.dto;

import com.kaloko.app.entity.ActivityLevel;
import com.kaloko.app.entity.Gender;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Float currentWeight,
        Float weightGoal,
        Float height,
        Integer age,
        ActivityLevel activityLevel,
        Gender gender,
        Float bodyFatPercentage,
        Integer dailyCalories,
        Integer proteinGoal,
        Integer carbGoal,
        Integer fatGoal
) {}

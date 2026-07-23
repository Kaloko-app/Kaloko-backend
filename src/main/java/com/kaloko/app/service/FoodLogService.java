package com.kaloko.app.service;

import com.kaloko.app.dto.FoodLogRequestDTO;
import com.kaloko.app.dto.FoodLogResponseDTO;
import com.kaloko.app.entity.Food;
import com.kaloko.app.entity.FoodLog;
import com.kaloko.app.entity.User;
import com.kaloko.app.repository.FoodLogRepository;
import com.kaloko.app.repository.FoodRepository;
import com.kaloko.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodLogService {

    private static final Pattern MICRONUTRIENT_PATTERN = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)\\s*([a-zA-Zµ]+)?$");

    private final FoodLogRepository foodLogRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Transactional
    public FoodLogResponseDTO logFood(FoodLogRequestDTO request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() -> new RuntimeException("Food not found"));

        FoodLog log = new FoodLog();
        log.setUser(user);
        log.setFood(food);
        log.setGrams(request.grams());
        log.setDate(request.date());
        log.setMealName(request.mealName());

        FoodLog savedLog = foodLogRepository.save(log);
        return convertToDTO(savedLog);
    }

    public List<FoodLogResponseDTO> getDailyLogs(LocalDate date) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return foodLogRepository.findByUserIdAndDateOrderByDateAsc(user.getId(), date)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public FoodLogResponseDTO updateFoodLog(Long id, Double grams) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        FoodLog log = foodLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));

        if (!log.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Unauthorized");
        }

        log.setGrams(grams);
        FoodLog updated = foodLogRepository.save(log);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteFoodLog(Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        FoodLog log = foodLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));

        if (!log.getUser().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Unauthorized");
        }

        foodLogRepository.delete(log);
    }

    private FoodLogResponseDTO convertToDTO(FoodLog log) {
        Food food = log.getFood();
        double multiplier = log.getGrams() / 100.0;

        int calories = (int) Math.round(food.getCalories() * multiplier);
        int protein = (int) Math.round(food.getProtein() * multiplier);
        int carbs = (int) Math.round(food.getCarbs() * multiplier);
        int fats = (int) Math.round(food.getFats() * multiplier);

        return new FoodLogResponseDTO(
                log.getId(),
                food.getId(),
                food.getName(),
                log.getGrams(),
                log.getDate(),
                log.getMealName(),
                calories,
                protein,
                carbs,
                fats,
                scaleMicronutrients(food.getMicronutrients(), multiplier)
        );
    }

    private Map<String, String> scaleMicronutrients(Map<String, String> original, double multiplier) {
        if (original == null) return Map.of();
        return original.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> scaleMicronutrientValue(entry.getValue(), multiplier)
                ));
    }

    private String scaleMicronutrientValue(String value, double multiplier) {
        if (value == null) return null;
        Matcher matcher = MICRONUTRIENT_PATTERN.matcher(value.trim());
        if (matcher.find()) {
            try {
                double numericValue = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2) != null ? matcher.group(2) : "";
                double scaledValue = numericValue * multiplier;
                
                String formatted = (scaledValue == (long) scaledValue) 
                        ? String.format("%d", (long) scaledValue) 
                        : String.format(java.util.Locale.US, "%.2f", scaledValue);
                // Strip trailing zeros if decimal
                if (formatted.contains(".")) {
                    formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
                }
                return formatted + unit;
            } catch (NumberFormatException e) {
                return value;
            }
        }
        return value;
    }
}

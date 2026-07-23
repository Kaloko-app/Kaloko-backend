package com.kaloko.app.service;

import com.kaloko.app.dto.FoodRequestDTO;
import com.kaloko.app.dto.FoodResponseDTO;
import com.kaloko.app.entity.Food;
import com.kaloko.app.entity.User;
import com.kaloko.app.repository.FoodRepository;
import com.kaloko.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public List<FoodResponseDTO> searchFoods(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return foodRepository.searchFoodsWithVisibility(query, user.getId())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public FoodResponseDTO createFood(FoodRequestDTO request) {
        Food food = new Food();
        food.setName(request.name());
        food.setBarcode(request.barcode());
        food.setCalories(request.calories());
        food.setProtein(request.protein());
        food.setCarbs(request.carbs());
        food.setFats(request.fats());
        if (request.micronutrients() != null) {
            food.setMicronutrients(request.micronutrients());
        }
        food.setIsPublic(request.isPublic() != null ? request.isPublic() : false);
        food.setServingSize(request.servingSize());
        food.setServingUnit(request.servingUnit());

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        food.setCreatedBy(user);

        Food saved = foodRepository.save(food);
        return convertToDTO(saved);
    }

    public List<FoodResponseDTO> getFavoriteFoods() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFavoriteFoods().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public boolean toggleFavorite(Long foodId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        boolean isFav = user.getFavoriteFoods().contains(food);
        if (isFav) {
            user.getFavoriteFoods().remove(food);
        } else {
            user.getFavoriteFoods().add(food);
        }
        userRepository.save(user);
        return !isFav;
    }

    private FoodResponseDTO convertToDTO(Food food) {
        return new FoodResponseDTO(
                food.getId(),
                food.getName(),
                food.getBarcode(),
                food.getCalories(),
                food.getProtein(),
                food.getCarbs(),
                food.getFats(),
                food.getMicronutrients() != null ? new HashMap<>(food.getMicronutrients()) : new HashMap<>(),
                food.getIsPublic(),
                food.getCreatedBy() != null ? food.getCreatedBy().getId() : null,
                food.getServingSize(),
                food.getServingUnit()
        );
    }
}

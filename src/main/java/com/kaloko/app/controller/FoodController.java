package com.kaloko.app.controller;

import com.kaloko.app.dto.FoodRequestDTO;
import com.kaloko.app.dto.FoodResponseDTO;
import com.kaloko.app.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/search")
    public ResponseEntity<List<FoodResponseDTO>> searchFoods(@RequestParam String query) {
        return ResponseEntity.ok(foodService.searchFoods(query));
    }

    @PostMapping
    public ResponseEntity<FoodResponseDTO> createFood(@RequestBody FoodRequestDTO request) {
        return ResponseEntity.ok(foodService.createFood(request));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FoodResponseDTO>> getFavorites() {
        return ResponseEntity.ok(foodService.getFavoriteFoods());
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.toggleFavorite(id));
    }
}

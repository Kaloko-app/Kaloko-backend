package com.kaloko.app.controller;

import com.kaloko.app.dto.FoodLogRequestDTO;
import com.kaloko.app.dto.FoodLogResponseDTO;
import com.kaloko.app.service.FoodLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/food-logs")
@RequiredArgsConstructor
public class FoodLogController {

    private final FoodLogService foodLogService;

    @PostMapping
    public ResponseEntity<FoodLogResponseDTO> logFood(@Valid @RequestBody FoodLogRequestDTO request) {
        return ResponseEntity.ok(foodLogService.logFood(request));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<FoodLogResponseDTO>> getDailyLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(foodLogService.getDailyLogs(date));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodLogResponseDTO> updateFoodLog(
            @PathVariable Long id,
            @RequestParam Double grams) {
        return ResponseEntity.ok(foodLogService.updateFoodLog(id, grams));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodLog(@PathVariable Long id) {
        foodLogService.deleteFoodLog(id);
        return ResponseEntity.noContent().build();
    }
}

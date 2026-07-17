package com.kaloko.app.service;

import com.kaloko.app.dto.FoodLogRequestDTO;
import com.kaloko.app.dto.FoodLogResponseDTO;
import com.kaloko.app.entity.Food;
import com.kaloko.app.entity.FoodLog;
import com.kaloko.app.entity.User;
import com.kaloko.app.repository.FoodLogRepository;
import com.kaloko.app.repository.FoodRepository;
import com.kaloko.app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodLogServiceTest {

    @Mock
    private FoodLogRepository foodLogRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FoodLogService foodLogService;

    private User testUser;
    private Food testFood;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testFood = new Food(1L, "Oats", "123", 389, 17, 66, 7, Map.of("Sal", "2g", "Vitamina B12", "5.5µg"), true, null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logFood_CreatesLog_AndReturnsResponseWithCalculatedMacros() {
        FoodLogRequestDTO request = new FoodLogRequestDTO(1L, 50.0, LocalDate.now(), "Breakfast");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(1L)).thenReturn(Optional.of(testFood));

        FoodLog mockSavedLog = new FoodLog();
        mockSavedLog.setId(100L);
        mockSavedLog.setUser(testUser);
        mockSavedLog.setFood(testFood);
        mockSavedLog.setGrams(50.0);
        mockSavedLog.setDate(request.date());
        mockSavedLog.setMealName("Breakfast");

        when(foodLogRepository.save(any(FoodLog.class))).thenReturn(mockSavedLog);

        FoodLogResponseDTO response = foodLogService.logFood(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(50.0, response.grams());
        assertEquals("Breakfast", response.mealName());
        
        // 50g is 0.5 of 100g. Calories: 389 * 0.5 = 195 (rounded)
        assertEquals(195, response.calories());
        assertEquals(9, response.protein()); // 17 * 0.5 = 8.5 -> 9
        assertEquals(33, response.carbs());  // 66 * 0.5 = 33
        assertEquals(4, response.fats());    // 7 * 0.5 = 3.5 -> 4
        assertEquals(Map.of("Sal", "1g", "Vitamina B12", "2.75µg"), response.micronutrients());
    }

    @Test
    void getDailyLogs_ReturnsLogsForGivenDate() {
        LocalDate today = LocalDate.now();
        FoodLog log1 = new FoodLog();
        log1.setId(10L);
        log1.setFood(testFood);
        log1.setGrams(100.0);
        log1.setDate(today);
        log1.setMealName("Breakfast");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(foodLogRepository.findByUserIdAndDateOrderByDateAsc(
                any(Long.class), any(LocalDate.class)))
                .thenReturn(List.of(log1));

        List<FoodLogResponseDTO> results = foodLogService.getDailyLogs(today);

        assertEquals(1, results.size());
        assertEquals("Breakfast", results.get(0).mealName());
        assertEquals(389, results.get(0).calories());
    }
}

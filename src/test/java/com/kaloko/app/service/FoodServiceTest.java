package com.kaloko.app.service;

import com.kaloko.app.dto.FoodRequestDTO;
import com.kaloko.app.dto.FoodResponseDTO;
import com.kaloko.app.entity.Food;
import com.kaloko.app.entity.User;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FoodService foodService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchFoods_ShouldPassUserIdToRepository() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Food mockFood = new Food();
        mockFood.setId(10L);
        mockFood.setName("Banana");
        mockFood.setIsPublic(true);
        when(foodRepository.searchFoodsWithVisibility("ban", 1L)).thenReturn(List.of(mockFood));

        List<FoodResponseDTO> results = foodService.searchFoods("ban");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Banana");
        verify(foodRepository).searchFoodsWithVisibility("ban", 1L);
    }

    @Test
    void createFood_ShouldAssignCreatorAndVisibility() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(foodRepository.save(any(Food.class))).thenAnswer(invocation -> {
            Food saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        FoodRequestDTO request = new FoodRequestDTO(
                "My Private Recipe",
                null,
                200, 10, 20, 5, null, false, null, null
        );

        FoodResponseDTO response = foodService.createFood(request);

        assertThat(response.isPublic()).isFalse();
        assertThat(response.createdById()).isEqualTo(1L);

        verify(foodRepository).save(argThat(food -> 
            food.getName().equals("My Private Recipe") &&
            food.getCreatedBy().getId().equals(1L) &&
            !food.getIsPublic()
        ));
    }
}

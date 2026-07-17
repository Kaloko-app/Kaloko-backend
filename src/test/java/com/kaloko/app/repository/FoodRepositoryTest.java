package com.kaloko.app.repository;

import com.kaloko.app.entity.Food;
import com.kaloko.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FoodRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodRepository foodRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass");
        user1.setEmail("user1@example.com");
        user1 = entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass");
        user2.setEmail("user2@example.com");
        user2 = entityManager.persistAndFlush(user2);

        Food publicFood = new Food();
        publicFood.setName("Public Apple");
        publicFood.setCalories(50);
        publicFood.setIsPublic(true);
        entityManager.persist(publicFood);

        Food privateFoodUser1 = new Food();
        privateFoodUser1.setName("User1 Private Apple");
        privateFoodUser1.setCalories(60);
        privateFoodUser1.setIsPublic(false);
        privateFoodUser1.setCreatedBy(user1);
        entityManager.persist(privateFoodUser1);

        Food privateFoodUser2 = new Food();
        privateFoodUser2.setName("User2 Private Apple");
        privateFoodUser2.setCalories(70);
        privateFoodUser2.setIsPublic(false);
        privateFoodUser2.setCreatedBy(user2);
        entityManager.persist(privateFoodUser2);

        entityManager.flush();
    }

    @Test
    void searchFoodsWithVisibility_ShouldReturnPublicAndOwnPrivateFoods() {
        List<Food> resultsUser1 = foodRepository.searchFoodsWithVisibility("Apple", user1.getId());
        
        assertThat(resultsUser1).hasSize(2);
        assertThat(resultsUser1).extracting(Food::getName)
                .containsExactlyInAnyOrder("Public Apple", "User1 Private Apple");

        List<Food> resultsUser2 = foodRepository.searchFoodsWithVisibility("Apple", user2.getId());
        
        assertThat(resultsUser2).hasSize(2);
        assertThat(resultsUser2).extracting(Food::getName)
                .containsExactlyInAnyOrder("Public Apple", "User2 Private Apple");
    }

    @Test
    void searchFoodsWithVisibility_ShouldCaseInsensitiveMatch() {
        List<Food> results = foodRepository.searchFoodsWithVisibility("aPplE", user1.getId());
        
        assertThat(results).hasSize(2);
    }
}

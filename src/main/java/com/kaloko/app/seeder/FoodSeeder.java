package com.kaloko.app.seeder;

import com.kaloko.app.entity.Food;
import com.kaloko.app.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodSeeder implements CommandLineRunner {

    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (foodRepository.count() > 0) {
            return;
        }

        List<Food> defaultFoods = List.of(
                createFood("Chicken Breast, raw", "100001", 110, 23, 0, 1),
                createFood("Chicken Breast, cooked", "100002", 165, 31, 0, 3),
                createFood("White Rice, raw", "100003", 360, 7, 80, 1),
                createFood("White Rice, cooked", "100004", 130, 2, 28, 0),
                createFood("Brown Rice, cooked", "100005", 112, 2, 23, 1),
                createFood("Rolled Oats", "100006", 389, 16, 66, 6),
                createFood("Banana", "100007", 89, 1, 22, 0),
                createFood("Apple", "100008", 52, 0, 13, 0),
                createFood("Avocado", "100009", 160, 2, 8, 14),
                createFood("Salmon, raw", "100010", 208, 20, 0, 13),
                createFood("Tuna, canned in water", "100011", 86, 19, 0, 0),
                createFood("Eggs, whole", "100012", 143, 12, 0, 9),
                createFood("Egg Whites", "100013", 52, 10, 0, 0),
                createFood("Whole Milk (3.5%)", "100014", 61, 3, 4, 3),
                createFood("Skim Milk (0%)", "100015", 34, 3, 5, 0),
                createFood("Greek Yogurt, 0% fat", "100016", 59, 10, 3, 0),
                createFood("Almonds", "100017", 579, 21, 21, 49),
                createFood("Peanut Butter", "100018", 588, 25, 20, 50),
                createFood("Olive Oil", "100019", 884, 0, 0, 100),
                createFood("Whey Protein Concentrate", "100020", 380, 75, 5, 6),
                createFood("Whey Protein Isolate", "100021", 370, 90, 1, 1),
                createFood("Sweet Potato, cooked", "100022", 90, 2, 20, 0),
                createFood("Potato, cooked", "100023", 87, 2, 20, 0),
                createFood("Broccoli, steamed", "100024", 35, 2, 7, 0),
                createFood("Spinach, raw", "100025", 23, 2, 3, 0)
        );

        foodRepository.saveAll(defaultFoods);
        System.out.println("Food database seeded with " + defaultFoods.size() + " items.");
    }

    private Food createFood(String name, String barcode, int calories, int protein, int carbs, int fats) {
        Food food = new Food();
        food.setName(name);
        food.setBarcode(barcode);
        food.setCalories(calories);
        food.setProtein(protein);
        food.setCarbs(carbs);
        food.setFats(fats);
        food.setIsPublic(true);
        food.setCreatedBy(null);
        return food;
    }
}

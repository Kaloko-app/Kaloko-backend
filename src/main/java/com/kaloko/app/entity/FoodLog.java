package com.kaloko.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import java.time.LocalDate;

/**
 * Log entry in the user's food diary for a specific meal.
 */
@Entity
@Table(name = "food_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "grams", nullable = false)
    @ToString.Include
    private Double grams;

    @Column(name = "date", nullable = false)
    @ToString.Include
    private LocalDate date;

    @Column(name = "meal_name", nullable = false)
    @ToString.Include
    private String mealName;

    @AssertTrue(message = "Exactly one of food or recipe must be specified, not both.")
    public boolean isValidFoodOrRecipe() {
        return (food != null) ^ (recipe != null);
    }

    @PrePersist
    @PreUpdate
    protected void validateExclusivity() {
        if (!isValidFoodOrRecipe()) {
            throw new IllegalStateException("Exactly one of food or recipe must be specified, not both.");
        }
    }
}

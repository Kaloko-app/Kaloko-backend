package com.kaloko.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents nutritional information of a food item per 100g.
 */
@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "barcode", unique = true)
    private String barcode;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein")
    private Integer protein;

    @Column(name = "carbs")
    private Integer carbs;

    @Column(name = "fats")
    private Integer fats;

    @ElementCollection
    @CollectionTable(name = "food_micronutrients", joinColumns = @JoinColumn(name = "food_id"))
    @MapKeyColumn(name = "nutrient_name")
    @Column(name = "amount")
    private Map<String, String> micronutrients = new HashMap<>();

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "serving_size")
    private Double servingSize;

    @Column(name = "serving_unit")
    private String servingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
}

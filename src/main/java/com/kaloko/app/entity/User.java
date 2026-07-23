package com.kaloko.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a user in the Kaloko platform.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    @ToString.Include
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    @ToString.Include
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "current_weight")
    private Float currentWeight;

    @Column(name = "weight_goal")
    private Float weightGoal;

    @Column(name = "height")
    private Float height;

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "body_fat_percentage")
    private Float bodyFatPercentage;

    @Column(name = "daily_calories")
    private Integer dailyCalories;

    @Column(name = "protein_goal")
    private Integer proteinGoal;

    @Column(name = "carb_goal")
    private Integer carbGoal;

    @Column(name = "fat_goal")
    private Integer fatGoal;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeightLog> weightLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BodyMeasurement> bodyMeasurements = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterLog> waterLogs = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorite_foods",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private Set<Food> favoriteFoods = new HashSet<>();
}

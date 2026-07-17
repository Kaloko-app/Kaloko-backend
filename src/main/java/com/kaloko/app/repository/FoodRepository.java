package com.kaloko.app.repository;

import com.kaloko.app.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository interface for Food entity.
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    Optional<Food> findByBarcode(String barcode);
    
    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')) AND (f.isPublic = true OR f.createdBy.id = :userId)")
    List<Food> searchFoodsWithVisibility(@Param("name") String name, @Param("userId") Long userId);
}

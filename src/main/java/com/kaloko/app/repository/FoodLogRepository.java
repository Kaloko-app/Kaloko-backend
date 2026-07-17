package com.kaloko.app.repository;

import com.kaloko.app.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for FoodLog entity.
 */
@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {
    
    List<FoodLog> findByUserIdAndDateOrderByDateAsc(Long userId, LocalDate date);
}

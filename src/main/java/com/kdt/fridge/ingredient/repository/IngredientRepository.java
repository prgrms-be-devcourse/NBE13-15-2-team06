package com.kdt.fridge.ingredient.repository;

import com.kdt.fridge.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
    @Query("""
        SELECT i
        FROM Ingredient i
        WHERE i.groupId = :groupId
          AND (i.shared = true OR i.ownerId = :userId)
        """)
    List<Ingredient> findVisibleIngredients(
            @Param("groupId") long groupId,
            @Param("userId") long userId
    );
}

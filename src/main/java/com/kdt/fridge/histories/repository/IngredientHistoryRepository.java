package com.kdt.fridge.histories.repository;

import com.kdt.fridge.histories.entity.IngredientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngredientHistoryRepository extends JpaRepository<IngredientHistory, Long> {

    // 냉장고에 속한 모든 식재료의 변경 이력, 최신순
    List<IngredientHistory> findByIngredientGroupIdOrderByCreatedAtDesc(Long groupId);

    @Query("""
        SELECT new com.kdt.fridge.ingredient.dto.IngredientHistoryResponse(
            h.id, u.id, h.action, h.quantityChange, h.detail, h.createdAt
        )
        FROM IngredientHistory h
        LEFT JOIN h.user u
        WHERE h.ingredient.id = :ingredientId
        ORDER BY h.createdAt DESC, h.id DESC
        """)
    List<com.kdt.fridge.ingredient.dto.IngredientHistoryResponse> findResponsesByIngredientId(
            @Param("ingredientId") long ingredientId
    );
}

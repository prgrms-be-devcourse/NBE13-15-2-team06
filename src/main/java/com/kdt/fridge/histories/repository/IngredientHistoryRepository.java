package com.kdt.fridge.histories.repository;

import com.kdt.fridge.histories.entity.IngredientHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientHistoryRepository extends JpaRepository<IngredientHistory, Long> {

    // 냉장고에 속한 모든 식재료의 변경 이력, 최신순
    List<IngredientHistory> findByIngredientGroupIdOrderByCreatedAtDesc(Long groupId);
}

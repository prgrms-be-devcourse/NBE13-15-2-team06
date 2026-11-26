package com.kdt.fridge.histories.dto;

import com.kdt.fridge.histories.entity.IngredientHistory;

import java.time.LocalDateTime;

public record IngredientHistoryResponse(
        Long id,
        String ingredientName,
        Long userId,
        String nickname,
        String action,
        Long quantityChange,
        String detail,
        LocalDateTime createdAt
) {
    public static IngredientHistoryResponse from(IngredientHistory history){
        return new IngredientHistoryResponse(
                history.getId(),
                history.getIngredient().getName(),
                history.getUser().getId(),
                history.getUser().getNickname(),
                history.getAction(),
                history.getQuantityChange(),
                history.getDetail(),
                history.getCreatedAt()
        );
    }
}

package com.kdt.fridge.ingredient.dto;

import java.time.LocalDateTime;

public record IngredientHistoryResponse (
    Long id,
    Long userId,
    String action,
    Long quantityChange,
    String detail,
    LocalDateTime createdAt
){

}

package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.NotNull;


public record IngredientStatusUpdateRequest(
    @NotNull(message = "변경할 식재료 상태를 입력해주세요.")
    Status status
){
    public enum Status{
        STORED,
        CONSUMED,
        DISCARDED
    }
}

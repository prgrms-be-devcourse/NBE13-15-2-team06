package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IngredientConsumeRequest(
    @NotNull(message = "소비할 수량을 입력해주세요.")
    @Positive(message = "소비할 수량은 1 이상이어야 합니다.")
    Long quantity
){
}

package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.NotNull;

public record IngredientOwnershipUpdateRequest(
        @NotNull(message = "공동 여부를 입력해주세요.")
        Boolean shared
) {
}

package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IngredientMemoUpdateRequest(
        @NotNull(message = "메모를 입력해주세요. 지우려면 빈 문자열을 보내주세요.")
        @Size(max = 255,message = "메모는 255자 이하여야 합니다.")
        String memo
) {
}

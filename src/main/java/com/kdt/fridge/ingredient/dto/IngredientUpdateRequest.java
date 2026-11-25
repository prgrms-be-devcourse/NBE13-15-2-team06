package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IngredientUpdateRequest(
        @Size(max = 50)
        @Pattern(regexp = "(?s).*\\S.*", message = "식재료 이름은 공백일 수 없습니다.")
        String name,

        @Size(max = 10) String unit,
        @Size(max = 20) String storageType,
        LocalDate purchasedAt,
        LocalDate expiresAt,
        @PositiveOrZero Integer notifyDaysBefore
) {
}

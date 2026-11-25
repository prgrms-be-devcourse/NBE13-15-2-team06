package com.kdt.fridge.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IngredientCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull @Positive Long quantity,
        @Size(max = 10) String unit,
        @Size(max = 20) String storageType,
        Boolean shared,
        LocalDate purchasedAt,
        LocalDate expiresAt,
        @PositiveOrZero Integer notifyDaysBefore,
        @Size(max = 255) String memo
) {
}

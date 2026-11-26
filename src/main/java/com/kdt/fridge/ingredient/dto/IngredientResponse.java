package com.kdt.fridge.ingredient.dto;

import com.kdt.fridge.ingredient.entity.Ingredient;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IngredientResponse(
        Long id,
        Long groupId,
        Long ownerId,
        String name,
        Long quantity,
        String unit,
        String storageType,
        boolean shared,
        LocalDate purchasedAt,
        LocalDate expiresAt,
        Integer notifyDaysBefore,
        String status,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static IngredientResponse from(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getGroupId(),
                ingredient.getOwnerId(),
                ingredient.getName(),
                ingredient.getQuantity(),
                ingredient.getUnit(),
                ingredient.getStorageType(),
                ingredient.isShared(),
                ingredient.getPurchasedAt(),
                ingredient.getExpiresAt(),
                ingredient.getNotifyDaysBefore(),
                ingredient.getStatus(),
                ingredient.getMemo(),
                ingredient.getCreatedAt(),
                ingredient.getUpdatedAt()
        );
    }
}
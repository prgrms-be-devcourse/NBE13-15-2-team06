package com.kdt.fridge.fridgeGroup.dto;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;

import java.time.LocalDateTime;

public record FridgeResponse(
        Long id,
        String name,
        String myRole,
        Long memberCount,
        LocalDateTime createdAt
) {
    public static FridgeResponse
    from(FridgeGroup fridge, String myRole, Long memberCount){
        return new FridgeResponse(
                fridge.getId(),
                fridge.getName(),
                myRole,memberCount,
                fridge.getCreatedAt());
    }
}

package com.kdt.fridge.fridgeGroup.dto;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;

public record FridgeSummaryResponse(
        Long id,
        String name,
        String myRole,
        Long memberCount
) {
    public static FridgeSummaryResponse
    from(FridgeGroup fridgeGroup, String myRole, Long memberCount){
        return new FridgeSummaryResponse(
                fridgeGroup.getId(),
                fridgeGroup.getName(),
                myRole,
                memberCount
        );
    }
}

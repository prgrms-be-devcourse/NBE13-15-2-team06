package com.kdt.fridge.fridgeGroup.dto;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;

public record InviteCodeResponse(
        String inviteCode
) {
    public static InviteCodeResponse from(FridgeGroup fridge) {
        return new InviteCodeResponse(fridge.getInviteCode());
    }
}
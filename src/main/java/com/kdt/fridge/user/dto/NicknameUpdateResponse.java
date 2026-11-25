package com.kdt.fridge.user.dto;

import com.kdt.fridge.user.entity.User;

public record NicknameUpdateResponse(Long userId, String nickname) {

    public static NicknameUpdateResponse from(User user) {
        return new NicknameUpdateResponse(user.getId(), user.getNickname());
    }
}
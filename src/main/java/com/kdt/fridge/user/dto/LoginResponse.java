package com.kdt.fridge.user.dto;

import com.kdt.fridge.user.entity.User;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String nickname,
        boolean nicknameRequired
) {

    public static LoginResponse of(User user, String accessToken, long expiresIn) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                user.getId(),
                user.getNickname(),
                user.isNicknameRequired()
        );
    }
}
package com.kdt.fridge.user.dto;

import com.kdt.fridge.user.entity.User;

public record KakaoLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        boolean isNewUser,
        String nickname,
        boolean nicknameRequired
) {

    public static KakaoLoginResponse of(User user, String accessToken, long expiresIn, boolean isNewUser) {
        return new KakaoLoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                user.getId(),
                isNewUser,
                user.getNickname(),
                user.isNicknameRequired()
        );
    }
}
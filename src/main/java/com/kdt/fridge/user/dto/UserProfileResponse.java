package com.kdt.fridge.user.dto;

import com.kdt.fridge.user.entity.LoginType;
import com.kdt.fridge.user.entity.User;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        LoginType loginType,
        LocalDateTime createdAt
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getCreatedAt()
        );
    }
}
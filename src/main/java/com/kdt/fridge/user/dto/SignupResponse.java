package com.kdt.fridge.user.dto;

import com.kdt.fridge.user.entity.User;

public record SignupResponse(Long userId, String email) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail());
    }
}
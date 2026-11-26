package com.kdt.fridge.user.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(

        @NotBlank(message = "인가 코드를 입력해 주세요.")
        String authorizationCode
) {
}
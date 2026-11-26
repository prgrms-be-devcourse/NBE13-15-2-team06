package com.kdt.fridge.groupmember.dto;

import jakarta.validation.constraints.NotBlank;

public record FridgeJoinRequest(

        @NotBlank(message = "초대 코드를 입력해 주세요.")
        String inviteCode
) {
}

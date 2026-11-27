package com.kdt.fridge.groupmember.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 냉장고 참여 신청 요청 DTO.
 * 참여하려는 냉장고의 초대 코드를 요청 본문으로 받는다. 비어 있거나 공백뿐이면 검증 단계에서 거부된다.
 *
 * @param inviteCode 참여할 냉장고의 초대 코드
 */
public record FridgeJoinRequest(

        @NotBlank(message = "초대 코드를 입력해 주세요.")
        String inviteCode
) {
}

package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.MemberRole;
import jakarta.validation.constraints.NotNull;

/**
 * 구성원 권한 변경 요청 DTO.
 * 구성원에게 새로 부여할 역할(ADMIN 또는 MEMBER)을 요청 본문으로 받으며, null이면 검증 단계에서 거부된다.
 *
 * @param role 변경할 역할
 */
public record MemberRoleUpdateRequest(

        @NotNull(message = "변경할 역할을 입력해 주세요.")
        MemberRole role
) {
}

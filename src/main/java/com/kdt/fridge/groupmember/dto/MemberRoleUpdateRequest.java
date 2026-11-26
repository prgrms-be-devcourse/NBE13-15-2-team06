package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.role.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(

        @NotNull(message = "변경할 역할을 입력해 주세요.")
        MemberRole role
) {
}

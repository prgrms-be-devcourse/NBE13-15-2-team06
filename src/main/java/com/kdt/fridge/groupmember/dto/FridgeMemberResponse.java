package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;

import java.time.LocalDateTime;

public record FridgeMemberResponse(
        Long memberId,
        Long userId,
        String nickname,
        MemberRole role,
        LocalDateTime joinedAt) {
    public static FridgeMemberResponse from(FridgeMember member) {
        return new FridgeMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}

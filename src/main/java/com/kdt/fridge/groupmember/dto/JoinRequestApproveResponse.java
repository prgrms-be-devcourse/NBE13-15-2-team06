package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import com.kdt.fridge.groupmember.entity.MemberRole;

import java.time.LocalDateTime;

public record JoinRequestApproveResponse(

        Long requestId,

        JoinRequestStatus status,

        Long memberId,

        Long userId,

        MemberRole role,

        LocalDateTime decidedAt
) {
    public static JoinRequestApproveResponse of(JoinRequest joinRequest, FridgeMember member) {
        return new JoinRequestApproveResponse(
                joinRequest.getId(),
                joinRequest.getStatus(),
                member.getId(),
                member.getUser().getId(),
                member.getRole(),
                joinRequest.getDecidedAt()
        );
    }
}

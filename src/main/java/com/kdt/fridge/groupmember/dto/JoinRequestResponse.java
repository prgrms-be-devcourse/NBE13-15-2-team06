package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;

import java.time.LocalDateTime;

public record JoinRequestResponse(

        Long requestId,

        Long fridgeId,
        String fridgeName,

        Long userId,
        String nickname,

        JoinRequestStatus status,

        LocalDateTime requestedAt,

        LocalDateTime decidedAt
) {
    public static JoinRequestResponse from(JoinRequest joinRequest) {
        return new JoinRequestResponse(
                joinRequest.getId(),

                joinRequest.getFridge().getId(),
                joinRequest.getFridge().getName(),
                joinRequest.getUser().getId(),
                joinRequest.getUser().getNickname(),
                joinRequest.getStatus(),
                joinRequest.getRequestedAt(),
                joinRequest.getDecidedAt()
        );
    }
}

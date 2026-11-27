package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import com.kdt.fridge.groupmember.entity.MemberRole;

import java.time.LocalDateTime;

/**
 * 참여 신청 수락 결과 응답 DTO.
 * 처리된 신청의 상태와, 그 결과로 새로 생성된 구성원 정보를 한 번에 돌려준다.
 *
 * @param requestId 처리된 참여 신청 ID
 * @param status    신청 상태(수락 후이므로 APPROVED)
 * @param memberId  새로 생성된 구성원(FridgeMember) ID
 * @param userId    새 구성원의 사용자 ID
 * @param role      새 구성원의 역할(수락 시 MEMBER)
 * @param decidedAt 수락 처리 시각
 */
public record JoinRequestApproveResponse(

        Long requestId,

        JoinRequestStatus status,

        Long memberId,

        Long userId,

        MemberRole role,

        LocalDateTime decidedAt
) {
    /**
     * 수락된 신청과 새로 만들어진 구성원 엔티티를 조합해 응답 DTO를 만든다.
     */
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

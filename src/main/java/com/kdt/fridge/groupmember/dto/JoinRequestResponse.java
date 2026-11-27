package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;

import java.time.LocalDateTime;

/**
 * 참여 신청 한 건의 정보를 응답하는 DTO.
 * 신청 생성, 대기 목록 조회, 거절 응답에서 공통으로 사용한다.
 *
 * @param requestId   참여 신청 ID
 * @param fridgeId    신청 대상 냉장고 ID
 * @param fridgeName  신청 대상 냉장고 이름
 * @param userId      신청자 사용자 ID
 * @param nickname    신청자 닉네임
 * @param status      신청 상태(PENDING/APPROVED/REJECTED)
 * @param requestedAt 신청한 시각
 * @param decidedAt   수락/거절된 시각. 아직 대기 중이면 null
 */
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
    /**
     * JoinRequest 엔티티를 응답 DTO로 변환한다. 냉장고·사용자 연관 엔티티를 함께 읽으므로
     * 트랜잭션 안에서 호출해야 LAZY 로딩 문제가 없다.
     */
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

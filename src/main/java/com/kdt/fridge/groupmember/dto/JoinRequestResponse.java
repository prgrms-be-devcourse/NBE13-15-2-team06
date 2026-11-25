package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.role.JoinRequestStatus;

import java.time.LocalDateTime;

/**
 * 참여 신청 1건의 정보를 클라이언트에게 돌려줄 때 쓰는 응답 객체입니다.
 * (참여 신청, 신청자 목록 조회, 신청 거절 API 에서 사용)
 *
 * [DTO 를 쓰는 이유]
 * 엔티티(JoinRequest)를 그대로 응답으로 내보내면 DB 구조와 불필요한 정보까지 노출됩니다.
 * 그래서 클라이언트에게 필요한 값만 골라 담은 DTO 로 바꿔서 내보냅니다.
 *
 * [record 란?]
 * 값만 담는 불변 객체를 짧게 만드는 자바 문법입니다.
 * 괄호 안의 이름들이 필드가 되고, 값을 꺼내는 메서드(requestId() 등)가 자동으로 생깁니다.
 * 이 객체는 JSON 으로 변환되어 응답 본문에 들어갑니다.
 */
public record JoinRequestResponse(

        // 신청 번호 (join_requests 테이블의 id)
        Long requestId,

        // 신청한 냉장고의 번호와 이름
        Long fridgeId,
        String fridgeName,

        // 신청한 회원의 번호와 닉네임
        Long userId,
        String nickname,

        // 신청 상태 (PENDING / APPROVED / REJECTED)
        JoinRequestStatus status,

        // 신청한 시각
        LocalDateTime requestedAt,

        // 수락/거절한 시각. 아직 처리 전이면 null 로 내려감
        LocalDateTime decidedAt
) {

    /**
     * 엔티티(JoinRequest)를 응답 객체로 변환한다.
     * static 메서드라서 JoinRequestResponse.from(joinRequest) 처럼 바로 호출할 수 있다.
     */
    public static JoinRequestResponse from(JoinRequest joinRequest) {
        return new JoinRequestResponse(
                joinRequest.getId(),
                // getFridge(), getUser() 는 연결된 엔티티. 여기서 처음 DB 조회가 일어남 (LAZY 로딩)
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

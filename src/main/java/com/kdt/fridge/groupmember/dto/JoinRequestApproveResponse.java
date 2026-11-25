package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.role.JoinRequestStatus;
import com.kdt.fridge.groupmember.role.MemberRole;

import java.time.LocalDateTime;

/**
 * 참여 신청을 수락했을 때 클라이언트에게 돌려주는 응답 객체입니다.
 * 처리된 신청 정보와, 수락 결과로 새로 생긴 구성원 정보를 함께 담습니다.
 *
 * [record 란?]
 * 값만 담는 불변 객체를 짧게 만드는 자바 문법입니다.
 * 괄호 안의 이름들이 필드가 되고, 값을 꺼내는 메서드가 자동으로 생깁니다.
 * 이 객체는 JSON 으로 변환되어 응답 본문에 들어갑니다.
 */
public record JoinRequestApproveResponse(

        // 처리된 신청의 번호
        Long requestId,

        // 수락 후의 신청 상태 (항상 APPROVED)
        JoinRequestStatus status,

        // 수락으로 새로 만들어진 구성원의 번호
        Long memberId,

        // 새 구성원이 된 회원의 번호
        Long userId,

        // 새 구성원의 역할 (수락하면 항상 MEMBER)
        MemberRole role,

        // 수락한 시각
        LocalDateTime decidedAt
) {

    /**
     * 신청(JoinRequest)과 새 구성원(FridgeMember), 두 개의 재료로 응답 객체를 만든다.
     * 재료가 하나일 때는 보통 from 이라는 이름을 쓰고, 여러 개일 때는 of 를 쓰는 관례가 있다.
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

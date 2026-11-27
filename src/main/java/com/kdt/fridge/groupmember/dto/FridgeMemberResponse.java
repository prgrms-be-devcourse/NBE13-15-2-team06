package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.entity.MemberRole;

import java.time.LocalDateTime;

/**
 * 냉장고 구성원 한 명의 정보를 응답하는 DTO.
 * 구성원 목록 조회와 권한 변경 응답에 사용된다.
 *
 * @param memberId 구성원(FridgeMember) 레코드 ID. 강퇴·권한 변경 시 경로에 쓰이는 값
 * @param userId   해당 구성원의 사용자 ID
 * @param nickname 사용자 닉네임
 * @param role     냉장고 내 역할(ADMIN/MEMBER)
 * @param joinedAt 냉장고에 가입한 시각
 */
public record FridgeMemberResponse(

        Long memberId,

        Long userId,

        String nickname,

        MemberRole role,

        LocalDateTime joinedAt
) {
    /**
     * 엔티티를 응답 DTO로 변환한다. 사용자 정보(id, nickname)를 함께 꺼내므로
     * LAZY 로딩된 user에 접근한다는 점에 유의한다(트랜잭션 안에서 호출해야 함).
     */
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

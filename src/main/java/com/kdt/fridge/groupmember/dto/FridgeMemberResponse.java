package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.entity.FridgeMember;
import com.kdt.fridge.groupmember.role.MemberRole;

import java.time.LocalDateTime;

/**
 * 구성원 1명의 정보를 클라이언트에게 돌려줄 때 쓰는 응답 객체입니다.
 * (구성원 조회, 권한 변경 API 에서 사용)
 *
 * [DTO 를 쓰는 이유]
 * 엔티티(FridgeMember)를 그대로 응답으로 내보내면 DB 구조와 불필요한 정보까지 노출됩니다.
 * 그래서 클라이언트에게 필요한 값만 골라 담은 DTO 로 바꿔서 내보냅니다.
 *
 * [record 란?]
 * 값만 담는 불변 객체를 짧게 만드는 자바 문법입니다.
 * 괄호 안의 이름들이 필드가 되고, 값을 꺼내는 메서드(memberId() 등)가 자동으로 생깁니다.
 * 이 객체는 JSON 으로 변환되어 응답 본문에 들어갑니다.
 */
public record FridgeMemberResponse(

        // 구성원 번호 (group_members 테이블의 id). 추방, 권한 변경 API 주소의 {memberId} 자리에 넣는 값
        Long memberId,

        // 회원 번호 (users 테이블의 id)
        Long userId,

        // 회원의 닉네임
        String nickname,

        // 구성원의 역할 (ADMIN 또는 MEMBER)
        MemberRole role,

        // 구성원이 된 시각
        LocalDateTime joinedAt
) {

    /**
     * 엔티티(FridgeMember)를 응답 객체로 변환한다.
     * static 메서드라서 FridgeMemberResponse.from(member) 처럼 바로 호출할 수 있다.
     */
    public static FridgeMemberResponse from(FridgeMember member) {
        return new FridgeMemberResponse(
                member.getId(),
                // member.getUser() 는 연결된 회원 엔티티. 여기서 처음 DB 조회가 일어남 (LAZY 로딩)
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}

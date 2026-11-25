package com.kdt.fridge.groupmember.dto;

import com.kdt.fridge.groupmember.role.MemberRole;
import jakarta.validation.constraints.NotNull;

/**
 * 구성원 권한 변경 API 에서 클라이언트가 보내는 요청 본문(JSON)을 담는 객체입니다.
 * 예) { "role": "ADMIN" }  → 관리자 권한 부여
 *     { "role": "MEMBER" } → 관리자 권한 회수
 *
 * [DTO(Data Transfer Object)란?]
 * 요청/응답으로 주고받는 데이터를 담는 전용 객체입니다.
 *
 * [record 란?]
 * 값만 담는 불변 객체를 짧게 만드는 자바 문법입니다.
 * 괄호 안의 이름(role)이 필드가 되고, 값을 꺼내는 메서드 role() 이 자동으로 생깁니다.
 */
public record MemberRoleUpdateRequest(

        // @NotNull: 값이 null 이면(= JSON 에 role 이 없으면) 검증 실패 → 400 Bad Request 에러
        //   (@NotBlank 는 문자열 전용이라, enum 에는 @NotNull 을 사용함)
        //   검증은 컨트롤러에서 @Valid 를 붙였을 때 실행됨
        @NotNull(message = "변경할 역할을 입력해 주세요.")
        MemberRole role
) {
}

package com.kdt.fridge.groupmember.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 참여 신청 API 에서 클라이언트가 보내는 요청 본문(JSON)을 담는 객체입니다.
 * 예) { "inviteCode": "X7K2PQ9M" }
 *
 * [DTO(Data Transfer Object)란?]
 * 요청/응답으로 주고받는 데이터를 담는 전용 객체입니다.
 * 엔티티를 그대로 주고받으면 DB 구조가 밖으로 드러나고 위험하므로, 필요한 값만 담은 DTO 를 따로 만듭니다.
 *
 * [record 란?]
 * 값만 담는 불변(바뀌지 않는) 객체를 아주 짧게 만드는 자바 문법입니다.
 * 괄호 안에 적은 이름(inviteCode)이 곧 필드가 되고, 생성자와 값을 꺼내는 메서드(inviteCode())가 자동으로 생깁니다.
 */
public record FridgeJoinRequest(

        // @NotBlank: 검증(Validation) 어노테이션
        //   null, 빈 문자열(""), 공백만 있는 문자열("   ")이면 검증 실패 → 400 Bad Request 에러
        //   검증은 컨트롤러에서 @Valid 를 붙였을 때 실행됨
        //   message: 검증에 실패했을 때 클라이언트에게 보여 줄 에러 메시지
        @NotBlank(message = "초대 코드를 입력해 주세요.")
        String inviteCode
) {
}

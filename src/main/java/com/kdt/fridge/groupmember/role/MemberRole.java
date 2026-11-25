package com.kdt.fridge.groupmember.role;

/**
 * 냉장고 구성원의 역할(등급)을 나타내는 enum 입니다.
 *
 * [enum 이란?]
 * "미리 정해 둔 값 중에서만 고를 수 있게" 해 주는 자바 문법입니다.
 * 문자열("admin", "ADMIN", "Admin" ...)로 역할을 다루면 오타가 나기 쉬운데,
 * enum 을 쓰면 아래 두 값 외에는 코드에서 쓸 수 없어서 실수를 컴파일 단계에서 잡아 줍니다.
 */
public enum MemberRole {

    // 관리자: 참여 신청 수락/거절, 구성원 추방, 권한 변경을 할 수 있음
    ADMIN,

    // 일반 구성원: 구성원 조회와 그룹 탈퇴만 할 수 있음
    MEMBER
}

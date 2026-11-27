package com.kdt.fridge.groupmember.entity;

/**
 * 냉장고 안에서 구성원이 가지는 역할.
 */
public enum MemberRole {
    // 운영자: 구성원 강퇴, 권한 변경, 참여 신청 수락/거절 등 관리 기능을 사용할 수 있다.
    ADMIN,

    // 일반 구성원: 냉장고를 이용만 할 수 있고 관리 권한은 없다.
    MEMBER
}

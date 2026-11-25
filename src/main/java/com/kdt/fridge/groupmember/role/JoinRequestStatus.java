package com.kdt.fridge.groupmember.role;

/**
 * 참여 신청이 지금 어떤 상태인지를 나타내는 enum 입니다.
 *
 * [상태가 바뀌는 흐름]
 *   신청 직후 PENDING → 관리자가 수락하면 APPROVED
 *                     → 관리자가 거절하면 REJECTED
 * 한 번 APPROVED / REJECTED 가 된 신청은 다시 처리할 수 없습니다.
 *
 * [enum 이란?]
 * "미리 정해 둔 값 중에서만 고를 수 있게" 해 주는 자바 문법입니다.
 * 문자열로 상태를 다루면 오타가 나기 쉬워서 enum 을 사용합니다.
 */
public enum JoinRequestStatus {

    // 대기 중: 신청만 하고 아직 관리자가 처리하지 않은 상태
    PENDING,

    // 수락됨: 관리자가 승인해서 구성원이 된 상태
    APPROVED,

    // 거절됨: 관리자가 거절한 상태 (기록은 지우지 않고 남겨 둠)
    REJECTED
}

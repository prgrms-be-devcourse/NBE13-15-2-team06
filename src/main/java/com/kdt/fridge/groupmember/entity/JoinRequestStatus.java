package com.kdt.fridge.groupmember.entity;

/**
 * 참여 신청의 처리 상태. PENDING에서 APPROVED 또는 REJECTED로 한 번만 바뀔 수 있다.
 */
public enum JoinRequestStatus {
    // 대기 중: 신청만 접수되고 운영자가 아직 처리하지 않음
    PENDING,

    // 수락됨: 운영자가 수락해 신청자가 구성원이 됨
    APPROVED,

    // 거절됨: 운영자가 거절해 구성원이 되지 않음
    REJECTED
}

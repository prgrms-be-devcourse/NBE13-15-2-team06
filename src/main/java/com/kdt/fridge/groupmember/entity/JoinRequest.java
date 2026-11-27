package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 냉장고 참여 신청 엔티티.
 * 사용자가 초대 코드로 특정 냉장고에 들어가고 싶다고 요청한 기록이며, 운영자가 수락/거절하면
 * 상태(status)와 처리 시각(decidedAt)이 갱신된다. 이미 처리된 신청은 다시 처리할 수 없다.
 * 테이블 이름은 {@code join_requests}이다.
 */
@Entity
@Table(name = "join_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinRequest {
    // 참여 신청 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 참여를 신청한 대상 냉장고 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private FridgeGroup fridge;

    // 참여를 신청한 사용자 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신청 상태(PENDING/APPROVED/REJECTED). 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(nullable = false)
    // 신청 접수 시각. 객체 생성 시점에 자동 기록되며 대기 목록 정렬 기준으로 쓰인다.
    private LocalDateTime requestedAt;

    // 수락/거절 처리 시각. 아직 대기 중이면 null
    private LocalDateTime decidedAt;

    /**
     * 외부에서 직접 생성하지 못하도록 막은 생성자. 항상 PENDING 상태, 현재 시각을 신청 시각으로 초기화한다.
     */
    private JoinRequest(FridgeGroup fridge, User user) {
        this.fridge = fridge;
        this.user = user;
        this.status = JoinRequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * 대기(PENDING) 상태의 새 참여 신청을 만든다.
     */
    public static JoinRequest create(FridgeGroup fridge, User user) {
        return new JoinRequest(fridge, user);
    }

    /**
     * 신청을 수락 상태(APPROVED)로 바꾸고 처리 시각을 기록한다.
     * 구성원 추가는 이 메서드의 일이 아니라 서비스에서 따로 수행한다.
     * 이미 처리된 신청이면 {@code JOIN_REQUEST_ALREADY_PROCESSED} 예외가 발생한다.
     */
    public void approve() {
        validatePending();
        this.status = JoinRequestStatus.APPROVED;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * 신청을 거절 상태(REJECTED)로 바꾸고 처리 시각을 기록한다.
     * 이미 처리된 신청이면 {@code JOIN_REQUEST_ALREADY_PROCESSED} 예외가 발생한다.
     */
    public void reject() {
        validatePending();
        this.status = JoinRequestStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    /**
     * 신청이 아직 대기(PENDING) 상태인지 확인한다. 수락/거절이 중복 처리되는 것을 막는 공통 검사.
     */
    private void validatePending() {
        if (this.status != JoinRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }
    }
}
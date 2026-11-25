package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.role.JoinRequestStatus;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 참여 신청 1건을 나타내는 엔티티입니다. (DB 의 join_requests 테이블의 한 줄)
 * "누가 / 어느 냉장고에 / 언제 신청했고 / 지금 상태가 무엇인지"를 저장합니다.
 *
 * [엔티티(Entity)란?]
 * DB 테이블 한 개를 자바 클래스 한 개로 표현한 것입니다.
 * 클래스의 필드 하나가 테이블의 컬럼 하나가 되고, 객체 하나가 테이블의 행(row) 하나가 됩니다.
 * JPA 가 이 클래스를 보고 SQL(INSERT, SELECT 등)을 대신 만들어 실행해 줍니다.
 *
 * 같은 사람이 거절당한 뒤 다시 신청할 수 있어야 해서,
 * (냉장고, 회원) 조합에 유일 제약(unique)은 걸지 않았습니다.
 */
// @Entity: "이 클래스는 DB 테이블과 연결되는 엔티티다"라고 JPA 에게 알려 주는 표시
@Entity
// @Table: 연결할 DB 테이블의 이름을 직접 지정 (생략하면 클래스 이름이 기준이 됨)
@Table(name = "join_requests")
// @Getter: Lombok 이 모든 필드의 getXxx() 메서드를 자동으로 만들어 줌 (예: getStatus())
//          값을 바꾸는 setter 는 일부러 만들지 않음 → 아래의 approve(), reject() 같은 메서드로만 값을 바꾸게 하기 위해서
@Getter
// @NoArgsConstructor: 파라미터가 없는 기본 생성자를 Lombok 이 만들어 줌
//   - JPA 는 DB 에서 읽어 온 데이터로 객체를 만들 때 기본 생성자가 반드시 필요함
//   - access = PROTECTED 로 해서 개발자가 실수로 new JoinRequest() 를 쓰지 못하게 막음
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinRequest {

    // @Id: 이 필드가 테이블의 기본키(Primary Key, 행을 구분하는 고유 번호)라는 표시
    @Id
    // @GeneratedValue: 기본키 값을 우리가 직접 넣지 않고 자동으로 만들어 달라는 뜻
    //   IDENTITY 방식 = DB 의 AUTO_INCREMENT 기능을 써서 1, 2, 3... 순서로 번호를 붙임
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: "여러 신청(Many)이 냉장고 한 개(One)에 속한다"는 관계 표시 (N:1 관계)
    //   fetch = LAZY: 신청을 조회할 때 냉장고 정보를 바로 가져오지 않고,
    //                 getFridge() 처럼 실제로 꺼내 쓸 때 조회함 (불필요한 조회를 줄여 성능이 좋아짐)
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn: 두 테이블을 연결하는 외래키(Foreign Key) 컬럼의 이름을 지정
    //   name = "group_id": join_requests 테이블의 group_id 컬럼에 냉장고 번호가 저장됨
    //   nullable = false: 이 값은 비어 있으면 안 됨 (NOT NULL)
    @JoinColumn(name = "group_id", nullable = false)
    private FridgeGroup fridge;

    // 신청한 회원. 위와 같은 방식으로 users 테이블과 연결됨 (user_id 컬럼)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @Enumerated: enum 값을 DB 에 어떤 형태로 저장할지 정하는 설정
    //   EnumType.STRING = "PENDING" 같은 글자 그대로 저장
    //   (기본값인 ORDINAL 은 0, 1, 2 같은 순서 번호로 저장하는데, enum 순서가 바뀌면 데이터가 꼬여서 위험함)
    @Enumerated(EnumType.STRING)
    // @Column: 컬럼의 세부 설정. nullable = false 는 NOT NULL, length = 20 은 최대 20글자
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    // 신청한 시각. 자바의 camelCase 이름(requestedAt)은 DB 에서 requested_at 으로 자동 변환됨
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // 관리자가 수락/거절한 시각.
    // 아직 처리 전에는 값이 없으므로(null) @Column(nullable = false) 를 붙이지 않음
    private LocalDateTime decidedAt;

    // 생성자를 private 으로 막아서, 아래의 create() 메서드로만 객체를 만들 수 있게 함
    private JoinRequest(FridgeGroup fridge, User user) {
        this.fridge = fridge;
        this.user = user;
        // 새 신청은 항상 "대기 중"으로 시작함 (바깥에서 상태를 마음대로 정할 수 없음)
        this.status = JoinRequestStatus.PENDING;
        // LocalDateTime.now() = 지금 시각
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * 새 참여 신청을 만든다.
     * static 메서드라서 객체 없이 JoinRequest.create(...) 로 바로 호출할 수 있다.
     * (정적 팩토리 메서드: 생성자 대신 이름 있는 메서드로 객체를 만드는 방식)
     */
    public static JoinRequest create(FridgeGroup fridge, User user) {
        return new JoinRequest(fridge, user);
    }

    /** 신청을 수락한다. 대기 중인 신청만 수락할 수 있다. */
    public void approve() {
        // 먼저 "대기 중인지" 검사. 이미 처리된 신청이면 여기서 예외가 터져서 아래 줄은 실행되지 않음
        validatePending();
        this.status = JoinRequestStatus.APPROVED;
        this.decidedAt = LocalDateTime.now();
    }

    /** 신청을 거절한다. 대기 중인 신청만 거절할 수 있다. */
    public void reject() {
        validatePending();
        this.status = JoinRequestStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    // 대기 중(PENDING)이 아니면 "이미 처리된 신청"이라는 예외를 던진다. (HTTP 409 Conflict)
    // private: 이 클래스 안에서만 쓰는 도우미 메서드
    private void validatePending() {
        if (this.status != JoinRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }
    }
}

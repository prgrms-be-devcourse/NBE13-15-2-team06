package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.groupmember.role.MemberRole;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 냉장고 구성원 1명을 나타내는 엔티티입니다. (DB 의 group_members 테이블의 한 줄)
 * "어떤 회원이 / 어느 냉장고에 / 어떤 역할(관리자, 일반)로 속해 있는지"를 저장합니다.
 *
 * [엔티티(Entity)란?]
 * DB 테이블 한 개를 자바 클래스 한 개로 표현한 것입니다.
 * 필드 하나가 컬럼 하나, 객체 하나가 행(row) 하나에 대응합니다.
 */
// @Entity: "이 클래스는 DB 테이블과 연결되는 엔티티다"라고 JPA 에게 알려 주는 표시
@Entity
// @Table: 연결할 테이블의 이름과 제약 조건을 지정
@Table(
        name = "group_members",
        // @UniqueConstraint: 지정한 컬럼 조합이 중복되지 못하게 하는 유일(unique) 제약
        //   → (group_id, user_id) 조합은 한 번만 존재할 수 있음
        //   → 같은 냉장고에 같은 회원이 두 번 등록되는 것을 DB 차원에서 막아 줌
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"})
)
// @Getter: Lombok 이 모든 필드의 getXxx() 메서드를 자동으로 만들어 줌 (setter 는 만들지 않음)
@Getter
// @NoArgsConstructor: JPA 가 객체를 만들 때 필요한 기본 생성자를 만들어 줌
//   access = PROTECTED: 개발자가 실수로 new FridgeMember() 를 쓰지 못하게 막음
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeMember {

    // @Id: 기본키(Primary Key, 행을 구분하는 고유 번호)
    @Id
    // @GeneratedValue(IDENTITY): 번호를 DB 의 AUTO_INCREMENT 로 자동 생성 (1, 2, 3...)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: "여러 구성원(Many)이 냉장고 한 개(One)에 속한다"는 관계 (N:1)
    //   fetch = LAZY: 실제로 getFridge() 를 호출할 때까지 냉장고 조회를 미룸 (성능 향상)
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn: 외래키(Foreign Key) 컬럼 이름 지정. group_id 컬럼에 냉장고 번호가 저장됨
    //   nullable = false: 비어 있으면 안 됨 (NOT NULL)
    @JoinColumn(name = "group_id", nullable = false)
    // 주의: 필드 이름 "fridge" 는 FridgeMemberRepository 의 메서드 이름(findByFridgeId...)과 연결되므로
    //       함부로 바꾸면 안 됨
    private FridgeGroup fridge;

    // 구성원인 회원. users 테이블과 연결됨 (user_id 컬럼)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @Enumerated(STRING): enum 값을 "ADMIN" / "MEMBER" 같은 글자 그대로 DB 에 저장
    //   (기본값 ORDINAL 은 0, 1 같은 번호로 저장해서, enum 순서가 바뀌면 데이터가 꼬일 수 있어 위험함)
    @Enumerated(EnumType.STRING)
    // @Column: 컬럼 세부 설정. NOT NULL 이고 최대 20글자
    @Column(nullable = false, length = 20)
    private MemberRole role;

    // 구성원이 된 시각. 자바의 joinedAt 은 DB 에서 joined_at 컬럼으로 자동 변환됨
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    // 생성자를 private 으로 막아서, 아래의 createAdmin / createMember 로만 객체를 만들게 함
    private FridgeMember(FridgeGroup fridge, User user, MemberRole role) {
        this.fridge = fridge;
        this.user = user;
        this.role = role;
        // LocalDateTime.now() = 지금 시각
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * 관리자 구성원을 만든다. 냉장고를 만든 사람을 처음 등록할 때 쓴다.
     * static 메서드라서 객체 없이 FridgeMember.createAdmin(...) 으로 바로 호출할 수 있다.
     */
    public static FridgeMember createAdmin(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.ADMIN);
    }

    /** 일반 구성원을 만든다. 참여 신청이 수락됐을 때 쓴다. */
    public static FridgeMember createMember(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.MEMBER);
    }

    /** 역할을 바꾼다. (관리자 권한 부여/회수에서 사용) */
    public void changeRole(MemberRole role) {
        this.role = role;
    }

    /** 이 구성원이 관리자이면 true, 아니면 false 를 돌려준다. */
    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }
}

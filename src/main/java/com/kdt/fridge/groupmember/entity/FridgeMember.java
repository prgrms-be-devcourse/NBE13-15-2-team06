package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.groupmember.entity.MemberRole;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 냉장고(FridgeGroup)와 사용자(User)의 소속 관계를 나타내는 엔티티.
 * 한 사용자가 한 냉장고에 속한다는 사실과 그 안에서의 역할, 가입 시각을 저장한다.
 * {@code (group_id, user_id)} 유니크 제약으로 같은 사용자가 같은 냉장고에 중복 가입하는 것을 DB 차원에서 막는다.
 * 테이블 이름은 {@code group_members}이다.
 */
@Entity
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"group_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeMember {
    // 구성원 레코드 PK (사용자 ID가 아님)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속된 냉장고 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private FridgeGroup fridge;

    // 소속된 사용자 (지연 로딩)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 냉장고 내 역할(ADMIN/MEMBER). 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false)
    // 가입 시각. 객체 생성 시점에 자동 기록되며 구성원 목록 정렬 기준으로 쓰인다.
    private LocalDateTime joinedAt;

    /**
     * 외부에서 직접 생성하지 못하도록 막은 생성자. 역할과 함께 가입 시각을 현재 시각으로 초기화한다.
     * 생성은 {@link #createAdmin}, {@link #createMember} 팩토리 메서드를 사용한다.
     */
    private FridgeMember(FridgeGroup fridge, User user, MemberRole role) {
        this.fridge = fridge;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * 운영자(ADMIN) 권한의 구성원을 만든다. 냉장고를 새로 만든 사용자를 첫 운영자로 등록할 때 사용한다.
     */
    public static FridgeMember createAdmin(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.ADMIN);
    }

    /**
     * 일반 구성원(MEMBER) 권한의 구성원을 만든다. 참여 신청이 수락됐을 때 사용한다.
     */
    public static FridgeMember createMember(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.MEMBER);
    }

    /**
     * 구성원의 역할을 바꾼다. 마지막 운영자 보호 같은 규칙은 이 메서드가 아니라 서비스 계층에서 검사한다.
     */
    public void changeRole(MemberRole role) {
        this.role = role;
    }

    /**
     * 이 구성원이 운영자(ADMIN)인지 여부를 반환한다.
     */
    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }
}
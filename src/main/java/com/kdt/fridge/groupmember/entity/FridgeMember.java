package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id" , nullable = false)
    private FridgeGroup fridge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private FridgeMember(FridgeGroup fridge, User user, MemberRole role) {
        this.fridge = fridge;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    // 관리자
    public static FridgeMember createAdmin(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.ADMIN);
    }

    // 구성원
    public static FridgeMember createMember(FridgeGroup fridge, User user) {
        return new FridgeMember(fridge, user, MemberRole.MEMBER);
    }

    // 권한 바꾸기
    public void changeRole(MemberRole role) {
        this.role = role;
    }
}
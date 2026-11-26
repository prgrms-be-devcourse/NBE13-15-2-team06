package com.kdt.fridge.groupmember.entity;

import com.kdt.fridge.fridgeGroup.entity.FridgeGroup;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import com.kdt.fridge.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity

@Table(name = "join_requests")

@Getter

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinRequest {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "group_id", nullable = false)
    private FridgeGroup fridge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime decidedAt;

    private JoinRequest(FridgeGroup fridge, User user) {
        this.fridge = fridge;
        this.user = user;

        this.status = JoinRequestStatus.PENDING;

        this.requestedAt = LocalDateTime.now();
    }

    public static JoinRequest create(FridgeGroup fridge, User user) {
        return new JoinRequest(fridge, user);
    }

    public void approve() {
        validatePending();
        this.status = JoinRequestStatus.APPROVED;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject() {
        validatePending();
        this.status = JoinRequestStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    private void validatePending() {
        if (this.status != JoinRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }
    }
}

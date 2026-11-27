package com.kdt.fridge.notification.entity;

import com.kdt.fridge.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="notifications")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long ingredientId;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false,length = 30)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(length = 255)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    // createdAt, updatedAt은 BaseTimeEntity에서 상속

    //생성자
    public Notification(Long userId, Long groupId, Long ingredientId, NotificationType type, String message) {
        this.userId = userId;
        this.groupId = groupId;
        this.ingredientId = ingredientId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    //읽음 처리 매서드
    public void markAsRead(){
        this.isRead = true;
    }
}

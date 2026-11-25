package com.kdt.fridge.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="notifications")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Notification {

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

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    //생성자
    public Notification(Long userId, Long ingredientId, Long groupId, NotificationType type, String message) {
        this.userId = userId;
        this.ingredientId = ingredientId;
        this.groupId = groupId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    //읽음 처리 매서드
    public void markAsRead(){
        this.isRead = true;
    }
}

package com.kdt.fridge.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name="notification_settings")
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean receiveOthers;

    @Column(nullable = false)
    private LocalTime notifyTime;

    //생성자
    public NotificationSetting(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
        this.enabled = true;
        this.receiveOthers = true;
        this.notifyTime = LocalTime.of(9,0);
    }

    //설정 업데이트용 매서드
    public void updateSetting(boolean enabled, boolean receiveOthers) {
        this.enabled = enabled;
        this.receiveOthers = receiveOthers;
    }

    //시간 업데이트용 매서드
    public void updateTime(LocalTime notifyTime) {
        this.notifyTime = notifyTime;
    }
}

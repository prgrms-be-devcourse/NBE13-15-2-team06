package com.kdt.fridge.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    //알림 목록 조회 (전체)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(Long userId){
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList();
    }

    //알림 목록 조회 (그룹별)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getGroupNotifications(Long userId, Long groupId){
        return notificationRepository.findByUserIdAndGroupIdOrderByCreatedAtDesc(userId,groupId).stream().map(NotificationResponse::from).toList();
    }

    //알림 읽음 처리
    @Transactional
    public void markNotificationAsRead(Long userId, Long notificationId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(InvalidId);
        if (!userId.equals(notification.getUserId())){
            throw new Error
        }
        notification.markAsRead();
    }
}

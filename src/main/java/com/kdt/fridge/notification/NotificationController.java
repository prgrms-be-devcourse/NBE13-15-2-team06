package com.kdt.fridge.notification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {
    private final NotificationService notificationService;

    //알림 목록 전체 조회
    @GetMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public List<NotificationResponse> getNotifications(@AuthenticationPrincipal Long userId) {
        return notificationService.getAllNotifications(userId);
    }

    //알림 목록 그룹 조회
    @GetMapping("/fridges/{fridgeId}/notifications")
    @ResponseStatus(HttpStatus.OK)
    public List<NotificationResponse> getGroupNotifications(@AuthenticationPrincipal Long userId, @PathVariable Long fridgeId) {
        return notificationService.getGroupNotifications(userId, fridgeId);
    }

    //알림 읽음 처리
    @PatchMapping("/notifications/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readNotification(@AuthenticationPrincipal Long userId, @PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(userId, notificationId);
    }

    //그룹별 알림 설정 조회
    @GetMapping("/fridges/{fridgeId}/members/me/notification-settings")
    @ResponseStatus(HttpStatus.OK)
    public NotificationSettingResponse getNotificationSettings(@AuthenticationPrincipal Long userId, @PathVariable Long fridgeId) {
        return notificationService.getGroupNotificationSetting(userId, fridgeId);
    }

    //그룹별 알림 설정 수정
    @PatchMapping("/fridges/{fridgeId}/members/me/notification-settings")
    @ResponseStatus(HttpStatus.OK)
    public NotificationSettingResponse updateNotificationSetting(@AuthenticationPrincipal Long userId,
                                                                 @PathVariable Long fridgeId,
                                                                 @RequestBody @Valid NotificationSettingRequest settingRequest){
        return notificationService.updateNotificationSetting(userId, fridgeId, settingRequest);
    }

    //알림 시간 조회
    @GetMapping("/users/me/notification-time")
    @ResponseStatus(HttpStatus.OK)
    public NotificationTimeResponse getNotificationTime(@AuthenticationPrincipal Long userId) {
        return notificationService.getNotificationTime(userId);
    }

    //알림 시간 수정
    @PatchMapping("/users/me/notification-time")
    @ResponseStatus(HttpStatus.OK)
    public NotificationTimeResponse updateNotificationTime(@AuthenticationPrincipal Long userId,
                                                           @RequestBody @Valid NotificationTimeRequest settingRequest) {
        return notificationService.updateNotificationTime(userId, settingRequest);
    }


}

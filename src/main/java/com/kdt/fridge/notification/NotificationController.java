package com.kdt.fridge.notification;

import com.kdt.fridge.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Notification", description = "알림 · 알림 설정")
public class NotificationController {
    private final NotificationService notificationService;

    //알림 목록 전체 조회
    @Operation(summary = "알림 목록 조회", description = "내가 받은 모든 알림을 최신순으로 조회")
    @GetMapping("/notifications")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal Long userId) {
        return ApiResponse.of(HttpStatus.OK, "알림 목록 조회 성공",notificationService.getAllNotifications(userId));
    }

    //알림 목록 그룹 조회
    @Operation(summary = "냉장고별 알림 목록 조회", description = "해당 냉장고에서 받은 내 알림을 최신순으로 조회. 구성원만 가능")
    @GetMapping("/fridges/{fridgeId}/notifications")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NotificationResponse>> getGroupNotifications(@AuthenticationPrincipal Long userId, @PathVariable Long fridgeId) {
        return ApiResponse.of(HttpStatus.OK, "그룹별 알림 목록 조회 성공",notificationService.getGroupNotifications(userId, fridgeId));
    }

    //알림 읽음 처리
    @Operation(summary = "알림 읽음 처리", description = "본인 알림만 가능")
    @PatchMapping("/notifications/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readNotification(@AuthenticationPrincipal Long userId, @PathVariable Long notificationId) {
        notificationService.markNotificationAsRead(userId, notificationId);
    }

    //그룹별 알림 설정 조회
    @Operation(summary = "냉장고별 알림 설정 조회", description = "해당 냉장고에 대한 내 알림 설정 조회. 구성원만 가능")
    @GetMapping("/fridges/{fridgeId}/members/me/notification-settings")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationSettingResponse> getNotificationSettings(@AuthenticationPrincipal Long userId, @PathVariable Long fridgeId) {
        return ApiResponse.of(HttpStatus.OK, "그룹별 알림 설정 조회 성공",notificationService.getGroupNotificationSetting(userId, fridgeId));
    }

    //그룹별 알림 설정 수정
    @Operation(summary = "냉장고별 알림 설정 수정", description = "알림 수신 여부와 다른 구성원 식재료 알림 수신 여부 변경. 구성원만 가능")
    @PatchMapping("/fridges/{fridgeId}/members/me/notification-settings")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationSettingResponse> updateNotificationSetting(@AuthenticationPrincipal Long userId,
                                                                 @PathVariable Long fridgeId,
                                                                 @RequestBody @Valid NotificationSettingRequest settingRequest){
        return ApiResponse.of(HttpStatus.OK, "그룹별 알림 설정 수정 성공",notificationService.updateNotificationSetting(userId, fridgeId, settingRequest));
    }

    //알림 시간 조회
    @Operation(summary = "알림 수신 시간 조회", description = "설정이 없으면 기본값 09:00 반환")
    @GetMapping("/users/me/notification-time")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationTimeResponse> getNotificationTime(@AuthenticationPrincipal Long userId) {
        return ApiResponse.of(HttpStatus.OK, "사용자의 알림 수신 시간 조회 성공",notificationService.getNotificationTime(userId));
    }

    //알림 시간 수정
    @Operation(summary = "알림 수신 시간 수정", description = "소속된 모든 냉장고에 동일하게 적용. 소속된 냉장고가 없으면 불가")
    @PatchMapping("/users/me/notification-time")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationTimeResponse> updateNotificationTime(@AuthenticationPrincipal Long userId,
                                                           @RequestBody @Valid NotificationTimeRequest settingRequest) {
        return ApiResponse.of(HttpStatus.OK, "사용자의 알림 수신 시간 수정 성공",notificationService.updateNotificationTime(userId, settingRequest));
    }


}

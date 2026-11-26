package com.kdt.fridge.notification;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.groupmember.repository.FridgeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    //알림 목록 조회 (전체)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(Long userId){
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList();
    }

    //알림 목록 조회 (그룹별)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getGroupNotifications(Long userId, Long groupId){
        validateGroupMember(userId, groupId);
        return notificationRepository.findByUserIdAndGroupIdOrderByCreatedAtDesc(userId,groupId).stream().map(NotificationResponse::from).toList();
    }

    //그룹원 여부 확인 내부 매서드
    private void validateGroupMember(Long userId, Long groupId){
        if (!fridgeMemberRepository.existsByFridgeIdAndUserId(groupId, userId)){
            throw new BusinessException(ErrorCode.NOT_FRIDGE_MEMBER);
        }
    }

    //알림 읽음 처리
    @Transactional
    public void markNotificationAsRead(Long userId, Long notificationId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!userId.equals(notification.getUserId())){
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
        notification.markAsRead();
    }

    //그룹 알림 설정 조회
    @Transactional(readOnly = true)
    public NotificationSettingResponse getGroupNotificationSetting(Long userId, Long groupId){
        validateGroupMember(userId, groupId);
        NotificationSetting setting = notificationSettingRepository.findByUserIdAndGroupId(userId,groupId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));
        return NotificationSettingResponse.from(setting);
    }

    //그룹 알림 설정 변경
    @Transactional
    public NotificationSettingResponse updateNotificationSetting(Long userId, Long groupId, NotificationSettingRequest settingRequest){
        validateGroupMember(userId, groupId);
        NotificationSetting setting = notificationSettingRepository.findByUserIdAndGroupId(userId,groupId)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOTIFICATION_SETTING_NOT_FOUND));
        setting.updateSetting(settingRequest.enabled(),settingRequest.receiveOthers());
        return NotificationSettingResponse.from(setting);
    }

    //알림 시간 조회
    @Transactional(readOnly = true)
    public NotificationTimeResponse getNotificationTime(Long userId){
        LocalTime notifyTime = notificationSettingRepository
                .findFirstByUserId(userId)
                .map(NotificationSetting::getNotifyTime)
                .orElse(LocalTime.of(9, 0));
        return new NotificationTimeResponse(notifyTime);
    }

    //알림 시간 변경
    @Transactional
    public NotificationTimeResponse updateNotificationTime(Long userId, NotificationTimeRequest settingRequest){
        List<NotificationSetting> settings = notificationSettingRepository.findAllByUserId(userId);
        if (settings.isEmpty()){
            throw new BusinessException(ErrorCode.NOTIFICATION_GROUP_REQUIRED);
        }
        for (NotificationSetting setting : settings) {
            setting.updateTime(settingRequest.notifyTime());
        }
        return new NotificationTimeResponse(settingRequest.notifyTime());
    }

}

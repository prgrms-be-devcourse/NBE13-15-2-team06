package com.kdt.fridge.notification.dto;

import com.kdt.fridge.notification.entity.NotificationSetting;

import java.time.LocalTime;

public record NotificationSettingResponse(
    Long userId,
    Long groupId,
    Boolean enabled,
    Boolean receiveOthers,
    LocalTime notifyTime
    ) {

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.getUserId(),
                setting.getGroupId(),
                setting.isEnabled(),
                setting.isReceiveOthers(),
                setting.getNotifyTime()
        );
    }
}

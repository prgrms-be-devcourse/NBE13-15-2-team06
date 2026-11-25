package com.kdt.fridge.notification;

public record NotificationSettingRequest(
        boolean enabled,
        boolean receiveOthers
) {
}

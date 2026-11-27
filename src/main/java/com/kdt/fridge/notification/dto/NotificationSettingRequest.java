package com.kdt.fridge.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingRequest(
        @NotNull(message = "알림 활성화 여부를 입력해 주세요.") Boolean enabled,
        @NotNull(message = "구성원 알림 활성화 여부를 입력해 주세요.") Boolean receiveOthers
) {
}

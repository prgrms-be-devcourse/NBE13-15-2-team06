package com.kdt.fridge.notification;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record NotificationTimeRequest(
        @NotNull(message = "알림 시간을 입력해 주세요.")
        LocalTime notifyTime
) {
}

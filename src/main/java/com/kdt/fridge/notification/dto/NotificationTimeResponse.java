package com.kdt.fridge.notification.dto;

import java.time.LocalTime;

public record NotificationTimeResponse(
        LocalTime notifyTime
) {
}

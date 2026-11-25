package com.kdt.fridge.notification;

import java.time.LocalTime;

public record NotificationTimeResponse(
        LocalTime notifyTime
) {
}

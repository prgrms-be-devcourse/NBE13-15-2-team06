package com.kdt.fridge.notification;

import java.time.LocalTime;

public record NotificationTimeRequest(
        LocalTime notifyTime
) {
}

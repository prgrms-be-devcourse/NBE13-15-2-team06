package com.kdt.fridge.notification.dto;

import com.kdt.fridge.notification.entity.NotificationType;
import com.kdt.fridge.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        String message,
        Long groupId,
        Long ingredientId,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getGroupId(),
                notification.getIngredientId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

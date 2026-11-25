package com.kdt.fridge.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        String message,
        Long groupId,
        Long ingredientId,
        boolean isRead,
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

package com.kdt.fridge.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}

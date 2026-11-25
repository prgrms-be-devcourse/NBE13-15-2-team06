package com.kdt.fridge.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationTimeResponse, Integer> {
}

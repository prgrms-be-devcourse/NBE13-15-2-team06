package com.kdt.fridge.notification.repository;

import com.kdt.fridge.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndGroupIdOrderByCreatedAtDesc(Long userId, Long groupId);
}

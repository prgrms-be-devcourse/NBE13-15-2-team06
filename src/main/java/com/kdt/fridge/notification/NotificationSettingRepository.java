package com.kdt.fridge.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUserIdAndGroupId(Long userId, Long groupId);
    Optional<NotificationSetting> findFirstByUserId(Long userId);
    List<NotificationSetting> findAllByUserId(Long userId);
}

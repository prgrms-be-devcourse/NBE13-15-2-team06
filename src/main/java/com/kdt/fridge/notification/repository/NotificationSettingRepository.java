package com.kdt.fridge.notification.repository;

import com.kdt.fridge.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUserIdAndGroupId(Long userId, Long groupId);
    Optional<NotificationSetting> findFirstByUserId(Long userId);
    List<NotificationSetting> findAllByUserId(Long userId);

    void deleteByUserIdAndGroupId(Long userId, Long groupId);
}

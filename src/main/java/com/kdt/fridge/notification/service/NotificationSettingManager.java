package com.kdt.fridge.notification.service;

public interface NotificationSettingManager {
    void createSetting(Long userId,Long  groupId);
    void deleteSetting(Long userId,Long  groupId);
}

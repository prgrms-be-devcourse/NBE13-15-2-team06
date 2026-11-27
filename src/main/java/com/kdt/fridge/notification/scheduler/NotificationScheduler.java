package com.kdt.fridge.notification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    @Scheduled(cron="${notification.scheduler.cron}")
    public void checkAndSendNotification(){

    }
}

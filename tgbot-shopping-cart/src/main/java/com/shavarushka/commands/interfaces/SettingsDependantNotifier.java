package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SettingsDependantNotifier {
    boolean shouldNotify(Long userId, NotificationType type);
    void sendNotification(Long userNotifierId, Long userId, String message) throws TelegramApiException;
    
    enum NotificationType {
        PRODUCT_ADDED,
        PRODUCT_DELETED,
        CATEGORY_ADDED,
        CATEGORY_DELETED
    }
}

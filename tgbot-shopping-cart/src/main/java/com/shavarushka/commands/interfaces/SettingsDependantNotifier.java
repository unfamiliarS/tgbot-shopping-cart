package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SettingsDependantNotifier {
    
    default void notifyIfEnabled(Long userNotifierId, Long userToNotifyId, String message, NotificationType type) throws TelegramApiException {
        if (!userToNotifyId.equals(userNotifierId) && shouldNotify(userToNotifyId, type)) {
            sendNotification(userNotifierId, userToNotifyId, message);
        }
    }

    boolean shouldNotify(Long userId, NotificationType type);
    void sendNotification(Long userNotifierId, Long userId, String message) throws TelegramApiException;
    
    enum NotificationType {
        PRODUCT_ADDED,
        PRODUCT_DELETED,
        CATEGORY_ADDED,
        CATEGORY_DELETED,
        CONFIRMATION_OF_JOINING_THE_CART,
        REFUSING_OF_JOINING_THE_CART
    }
}

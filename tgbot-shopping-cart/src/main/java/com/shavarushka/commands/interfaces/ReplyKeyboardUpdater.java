package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface ReplyKeyboardUpdater {
    void updateReplyKeyboardOnDataChanges(Long userId, Long cartId) throws TelegramApiException;
}

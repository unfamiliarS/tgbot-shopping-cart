package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface ReplyKeyboardUpdater {
    void updateReplyKeyboard(Long userId, Long cartId) throws TelegramApiException;
}

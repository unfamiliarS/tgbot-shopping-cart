package com.shavarushka.commands.keyboard;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CartSelectionListener {
    void onCartSelected(Long userId, Long cartId) throws TelegramApiException;
}

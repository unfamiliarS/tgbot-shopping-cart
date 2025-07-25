package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.keyboard.CartSelectionListener;

public interface SelectedCartNotifier {
    void addCartSelectionListener(CartSelectionListener listener);
    void removeCartSelectionListener(CartSelectionListener listener);
    void notifyCartSelectionListeners(Long userId, Long cartId) throws TelegramApiException; 
}

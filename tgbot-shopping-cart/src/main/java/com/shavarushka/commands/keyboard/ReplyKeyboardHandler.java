package com.shavarushka.commands.keyboard;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class ReplyKeyboardHandler implements CartSelectionListener {
    protected final MessageSender sender;
    private final SQLiteConnection connection;

    public ReplyKeyboardHandler(MessageSender sender, SQLiteConnection connection, SelectedCartNotifier notifier) {
        this.sender = sender;
        this.connection = connection;
        notifier.addCartSelectionListener(this);
    }

    @Override
    public void onCartSelected(Long userId, Long cartId) {
        try {
            Long chatId = getChatIdByUserId(userId);
            
            if (chatId != null) {
                updateKeyboard(cartId, chatId, cartId != null);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Long getChatIdByUserId(Long userId) {
        Users user = connection.getUserById(userId);
        return user != null ? user.chatId() : null;
    }

    private void updateKeyboard(Long cartId, Long chatId, boolean hasCart) throws TelegramApiException {
        String cartName = connection.getCartById(cartId).cartName();
        ReplyKeyboard keyboard;
        
        if (hasCart) {
            keyboard = KeyboardsFabrics.createKeyboard(
                Map.of("/sort_by_cost", "Отсортировать по цене",
                    "/sort_by_date", "Отсортировать по дате добавления",
                    "/search", "Поиск"),
                2, ReplyKeyboardMarkup.class
            );
        } else {
            keyboard = new ReplyKeyboardRemove(true);
        }
        String message = "Вы в корзине: *" + MessageSender.escapeMarkdownV2(cartName) + "*";
        sender.sendMessage(chatId, message, keyboard, true);
    }
}
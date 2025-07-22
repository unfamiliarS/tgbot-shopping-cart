package com.shavarushka.commands.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;

public abstract class AbstractCommand implements BotCommand {
    protected final MessageSender sender;
    protected final Map<Long, BotState> userStates;
    protected final SQLiteConnection connection;

    public AbstractCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        this.sender = sender;
        this.userStates = userStates;
        this.connection = connection;
    }

    @Override
    public boolean shouldProcess(Update update) {
        Long chatId;
        String message;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            message = update.getMessage().getText();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            message = update.getCallbackQuery().getData();
        } else {
            return false;
        }

        return !userStates.containsKey(chatId) &&
                message.startsWith(getCommand().strip());
    }

    protected InlineKeyboardMarkup getProductKeyboard(Products product) {
        return KeyboardsFabrics.createKeyboard(
                Map.of(
                    "/purchasestatus_" + product.productId(), product.productPurchaseStatusAsString(),
                    "/changecategoryfor_" + product.productId(), "🔄 Сменить категорию",
                    "/deleteproduct_" + product.productId(), "🗑 Удалить"
                ),
                3,
                InlineKeyboardMarkup.class
            );
    }

    // base check ups
    protected boolean checkForUserExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getUserById(userId) == null) {
            message = "Не могу найти тебя в своей базе😔 Вероятно мы ещё не знакомы.\n/start чтобы поздороваться";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForCartExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getUserById(userId).selectedCartId() == null) {
            message = "У тебя нет ни одной корзины😔 \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForProductExisting(Long chatId, Integer messageId, Long productId) throws TelegramApiException {
        if (connection.getProductById(productId) == null) {
            sender.deleteMessage(chatId, messageId);
            return false;
        }
        return true;
    }
}

package com.shavarushka.commands.keyboard;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;

public class ReplyKeyboardHandler implements BotCommand {
    protected final MessageSender sender;
    private final SQLiteConnection connection;

    public ReplyKeyboardHandler(MessageSender sender, SQLiteConnection connection) {
        this.sender = sender;
        this.connection = connection;
    }

    public String getReplyKeyboardHandlerName() {
        return "keyboard_for_selected_cart";
    }

    // should override if need to check BotState 
    @Override
    public boolean shouldProcess(Update update) {
        Long userId = null;
        boolean isCartSelected = false;
        
        // check if user have selected cart
        if (update.hasMessage() && update.getMessage().hasText()) {
            userId = update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();    
        }

        if (userId != null && connection.getUserById(userId).selectedCartId() != null) {
            isCartSelected = true;
        }

        return isCartSelected;
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            return;
        }

        ReplyKeyboardMarkup keyboard = KeyboardsFabrics.createKeyboard(
            Map.of("/start", "/start"), 1, ReplyKeyboardMarkup.class
        );

        sender.sendMessage(chatId, "f", keyboard, false);
    }
}

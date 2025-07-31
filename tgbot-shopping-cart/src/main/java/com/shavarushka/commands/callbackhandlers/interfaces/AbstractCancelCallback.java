package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.database.SQLiteConnection;

public abstract class AbstractCancelCallback extends AbstractCallbackCommand {

    public AbstractCancelCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    public boolean shouldProcessCanceling(BotCommand command, Update update, BotState... states) {
        if (isThisCallback(update)) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String message = update.getCallbackQuery().getData();
            return isUserHaveState(chatId, states) && message.startsWith(command.getCommand().strip());
        }
        return false;
    }

    protected void processCanceling(Update update, String message) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message, false);
    }

    protected void processCanceling(Update update, String message, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message, keyboard, false);
    }  
}

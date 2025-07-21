package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.AbstractCommand;
import com.shavarushka.database.SQLiteConnection;

public abstract class AbstractCallbackCommand extends AbstractCommand {
    public AbstractCallbackCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    // should override if need to check BotState
    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();
        return !userStates.containsKey(chatId) &&
                callback.startsWith(getCommand().strip());
    }

    protected Long extractIdFromMessage(String message) {
        return Long.parseLong(message.substring(getCommand().length()));
    }
}
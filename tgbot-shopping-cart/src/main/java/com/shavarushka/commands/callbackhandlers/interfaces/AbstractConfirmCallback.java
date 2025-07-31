package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.database.SQLiteConnection;

public abstract class AbstractConfirmCallback extends AbstractCallbackCommand {

    public AbstractConfirmCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    public boolean shouldProcessConfirming(BotCommand command, Update update, BotState state) {
        if (isThisCallback(update)) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String message = update.getCallbackQuery().getData();
            return isUserHaveState(chatId, state) && message.startsWith(command.getCommand().strip());
        }
        return false;
    }
}

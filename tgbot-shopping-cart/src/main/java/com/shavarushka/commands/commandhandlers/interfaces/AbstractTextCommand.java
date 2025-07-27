package com.shavarushka.commands.commandhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.AbstractCommand;
import com.shavarushka.database.SQLiteConnection;

public abstract class AbstractTextCommand extends AbstractCommand {

    public AbstractTextCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    abstract public String getDescription();

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return false;

        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        return !userStates.containsKey(chatId) &&
                message.matches(getCommand().strip());
    }
}
package com.shavarushka.commands.commandhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.interfaces.AbstractCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractTextCommand extends AbstractCommand {
    public AbstractTextCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    abstract public String getDescription();

    // should override if need to check BotState 
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
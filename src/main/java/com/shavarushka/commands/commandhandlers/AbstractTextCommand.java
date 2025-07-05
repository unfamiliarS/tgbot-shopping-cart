package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.interfaces.TextCommand;

public abstract class AbstractTextCommand extends MessageSender implements TextCommand {
    protected final Map<Long, BotState> userStates;

    public AbstractTextCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient);
        this.userStates = userStates;
    }

    // should override if need to check BotState 
    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return false;

        long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        return userStates.get(chatId) == null &&
                message.matches(getCommand().strip());
    }
}
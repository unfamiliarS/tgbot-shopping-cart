package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotState;
import com.shavarushka.commands.intr.MessageSender;
import com.shavarushka.commands.intr.TextCommand;

public abstract class AbstractTextCommand extends MessageSender implements TextCommand {
    protected final Map<Long, BotState> userStates;

    public AbstractTextCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient);
        this.userStates = userStates;
    }

    // should override if need to check BotState 
    @Override
    public boolean shouldProcess(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            return update.getMessage().hasText() &&
                   userStates.get(chatId) == null &&
                   message.matches(getCommand().strip());
        }
        return false;
    }
}
package com.shavarushka.commands.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class AbstractCommand implements BotCommand {
    protected final MessageSender sender;
    protected final Map<Long, BotState> userStates;

    public AbstractCommand(MessageSender sender, Map<Long, BotState> userStates) {
        this.sender = sender;
        this.userStates = userStates;
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
}

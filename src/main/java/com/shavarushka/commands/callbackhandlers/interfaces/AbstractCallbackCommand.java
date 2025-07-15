package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.interfaces.AbstractCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractCallbackCommand extends AbstractCommand {
    public AbstractCallbackCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
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
}
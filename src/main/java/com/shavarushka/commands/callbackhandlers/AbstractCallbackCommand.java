package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.CallbackCommand;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractCallbackCommand implements CallbackCommand {
    protected final Map<Long, BotState> userStates;
    protected final MessageSender sender;
    
    public AbstractCallbackCommand(MessageSender sender, Map<Long, BotState> userStates) {
        this.sender = sender;        
        this.userStates = userStates;
    }
    
    // should override if need to check BotState
    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();
        return !userStates.containsKey(chatId) &&
                callback.startsWith(getCallbackPattern().strip());
    }
}
package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CancelInvitingUserCallback extends AbstractCancelCallback {
    public CancelInvitingUserCallback(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }
    
    @Override
    public String getCallbackPattern() {
        return "/cancelinvitinguser";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.WAITING_FOR_USERNAME_TO_INVITE);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message = "❌ Отменяю создание приглашения...";
        processCanceling(update, message);
    }

    
}

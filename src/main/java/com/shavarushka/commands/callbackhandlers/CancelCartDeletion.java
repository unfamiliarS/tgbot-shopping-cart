package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CancelCartDeletion extends AbstractCancelCallback {

    public CancelCartDeletion(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    @Override
    public String getCallbackPattern() {
        return "/cancelcartdeletion";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message = "❌ Отменяю удаление корзины...";
        processCanceling(update, message);
    }

}

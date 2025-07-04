package com.shavarushka.commands.callbackhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotState;

public class ConfirmCartCreationCallback extends AbstractCallbackCommand {

    public ConfirmCartCreationCallback(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
    }

    @Override
    public String getCallbackPattern() {
        return "/confirmcartcreation_"; // + cartName
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String cartName = update.getCallbackQuery().getData().substring(getCallbackPattern().length());
        String message = escapeMarkdownV2("Успешно! Добро пожаловать в ") + "*" + cartName + "*";
        userStates.remove(chatId);
        editMessage(chatId, messageId, message);
    }
}

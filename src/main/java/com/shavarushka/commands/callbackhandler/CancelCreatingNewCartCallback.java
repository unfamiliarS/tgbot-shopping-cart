package com.shavarushka.commands.callbackhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotState;

public class CancelCreatingNewCartCallback extends AbstractCallbackCommand {
    public CancelCreatingNewCartCallback(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
    }
    
    @Override
    public String getCallbackPattern() {
        return "/cancelcreatingnewcart";
    }

    @Override
    public boolean shouldProcess(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.hasCallbackQuery() &&
               update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               (userStates.get(chatId).equals(BotState.WAITING_FOR_CART_NAME) ||
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION));
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message = escapeMarkdownV2("Отменяю создание корзины...");
        userStates.remove(chatId);
        editMessage(chatId, messageId, message);
    }
}

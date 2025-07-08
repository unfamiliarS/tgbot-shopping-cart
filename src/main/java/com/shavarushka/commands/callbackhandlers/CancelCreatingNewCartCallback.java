package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CancelCreatingNewCartCallback extends AbstractCallbackCommand {
    public CancelCreatingNewCartCallback(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }
    
    @Override
    public String getCallbackPattern() {
        return "/cancelcreatingnewcart";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               (userStates.get(chatId).equals(BotState.WAITING_FOR_CART_NAME) ||
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION));
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message = "❌ " + MessageSender.escapeMarkdownV2("Отменяю создание корзины...");
        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message);
    }
}

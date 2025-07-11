package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractCancelCallback extends AbstractCallbackCommand {
    public AbstractCancelCallback(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    protected void processCanceling(Update update, String message) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message, false);
    }
}

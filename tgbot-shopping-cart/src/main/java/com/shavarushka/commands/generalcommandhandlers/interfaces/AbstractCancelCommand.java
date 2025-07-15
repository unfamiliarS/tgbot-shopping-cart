package com.shavarushka.commands.generalcommandhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.AbstractCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractCancelCommand extends AbstractCommand {
    public AbstractCancelCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    protected void processCanceling(Update update, String message) throws TelegramApiException {
        Long chatId;
        Integer messageId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        } else {
            return;
        }

        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message, false);
    }
}

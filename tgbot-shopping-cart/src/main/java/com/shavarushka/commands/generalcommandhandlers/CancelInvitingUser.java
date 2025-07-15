package com.shavarushka.commands.generalcommandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.generalcommandhandlers.interfaces.AbstractCancelCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CancelInvitingUser extends AbstractCancelCommand {
    public CancelInvitingUser(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }
    
    @Override
    public String getCommand() {
        return "/cancelinvitinguser";
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

        return message.startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.WAITING_FOR_USERNAME_TO_INVITE);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message = "❌ Отменяю создание приглашения...";
        processCanceling(update, message);
    }
}

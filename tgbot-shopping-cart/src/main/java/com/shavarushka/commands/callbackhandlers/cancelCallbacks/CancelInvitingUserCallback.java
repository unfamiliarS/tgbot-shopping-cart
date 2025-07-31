package com.shavarushka.commands.callbackhandlers.cancelCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.database.SQLiteConnection;

public class CancelInvitingUserCallback extends AbstractCancelCallback {
    public CancelInvitingUserCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }
    
    @Override
    public String getCommand() {
        return "/cancelinvitinguser";
    }

    @Override
    public boolean shouldProcess(Update update) {
        return shouldProcessCanceling(update, BotState.WAITING_FOR_USERNAME_TO_INVITE);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message = "❌ Отменяю создание приглашения...";
        processCanceling(update, message);
    }
}

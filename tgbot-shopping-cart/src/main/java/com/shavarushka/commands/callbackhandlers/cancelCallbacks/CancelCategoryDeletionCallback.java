package com.shavarushka.commands.callbackhandlers.cancelCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.database.SQLiteConnection;

public class CancelCategoryDeletionCallback extends AbstractCancelCallback {

    public CancelCategoryDeletionCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/cancelcategorydeletion_";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;
            
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String message = update.getCallbackQuery().getData();

        return message.startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CATEGORY_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message = "❌ Отменяю удаление категории...";
        processCanceling(update, message);
    }
}

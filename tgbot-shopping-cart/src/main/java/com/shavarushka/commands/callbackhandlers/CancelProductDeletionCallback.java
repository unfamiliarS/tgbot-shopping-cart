package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;

public class CancelProductDeletionCallback extends AbstractCancelCallback {
    public CancelProductDeletionCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }
    
    @Override
    public String getCommand() {
        return "/cancelproductdeletion_";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;
            
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String message = update.getCallbackQuery().getData();

        return message.startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_PRODUCT_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        String message;
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Products product = connection.getProductById(productId);
        message = connection.getProductById(productId).fullURL();
        var keyboard = getProductKeyboard(product);

        processCanceling(update, message, keyboard);
    }
}

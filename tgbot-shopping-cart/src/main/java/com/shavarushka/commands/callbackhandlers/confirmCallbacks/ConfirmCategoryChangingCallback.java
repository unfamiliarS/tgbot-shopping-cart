package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractConfirmCallback;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;

public class ConfirmCategoryChangingCallback extends AbstractConfirmCallback {
    
    public ConfirmCategoryChangingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/setcat_"; // + categoryId _ productId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        Long[] categoryId_productId = extractTwoIdFromMessage(update.getCallbackQuery().getData());
        Long categoryId = categoryId_productId[0];
        Long productId = categoryId_productId[1];
        Products product = connection.getProductById(productId);
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (!checkForProductExisting(chatId, messageId, productId) || !checkForCategoryExisting(chatId, messageId, categoryId)
            || !checkIfProductInCurrentUserCart(chatId, messageId, userId, productId))
            return;

        if (categoryId.equals(product.assignedCategoryId())) {
            message = product.fullURL();
            var keyboard = getProductKeyboard(product);
            sender.editMessage(chatId, messageId, message, keyboard, false);
        } else if (connection.updateCategoryForProduct(productId, categoryId)) {
            sender.deleteMessage(chatId, messageId);
        }                
    }
}

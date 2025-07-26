package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;

public class ChangePurchaseStatusCallback extends AbstractCallbackCommand {

    public ChangePurchaseStatusCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/purchasestatus_"; // + id
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Products product;

        if (!checkForProductExisting(chatId, messageId, productId) || checkIfProductInCurrentUserCart(chatId, messageId, userId, productId))
            return;

        product = connection.getProductById(productId);
        if (connection.updatePurchaseStatusForProduct(productId, !product.productPurchaseStatus())) {
            product = connection.getProductById(productId);
            message = product.fullURL();
            var keyboard = getProductKeyboard(product);
            sender.editMessage(chatId, messageId, message, keyboard, false);
        }
    }
}

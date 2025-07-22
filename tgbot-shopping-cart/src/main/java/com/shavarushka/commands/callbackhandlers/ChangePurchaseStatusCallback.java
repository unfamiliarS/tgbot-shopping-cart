package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
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
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Products product;
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (isProductExist(productId)) {
            product = connection.getProductById(productId);
            if (connection.updatePurchaseStatusForProduct(productId, !product.productPurchaseStatus())) {
                product = connection.getProductById(productId);
                String message = product.fullURL();
                var keyboard = KeyboardsFabrics.createKeyboard(
                    Map.of(
                        "/purchasestatus_" + productId, product.productPurchaseStatusAsString(),
                        "/changecategoryfor_" + productId, "Сменить категорию",
                        "/deleteproduct_" + productId, "🗑"
                    ), 
                    2,
                    InlineKeyboardMarkup.class
                );
                sender.editMessage(chatId, messageId, message, keyboard, false);
            }
        } else {
            sender.deleteMessage(chatId, messageId);
        }
    }

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
    }
}

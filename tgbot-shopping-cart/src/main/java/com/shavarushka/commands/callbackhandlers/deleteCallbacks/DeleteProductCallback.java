package com.shavarushka.commands.callbackhandlers.deleteCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class DeleteProductCallback extends AbstractCallbackCommand {
    public DeleteProductCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/deleteproduct_"; // + productId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        Products product;
        
        if (!isProductExist(productId)) {
            sender.deleteMessage(chatId, messageId);
            System.out.println("product don't exist");
        } else if (!checkIfProductInCurrentUserCart(chatId, messageId, user.userId(), productId)) {
            System.out.println("product don't assigned to current user cart");
        } else {
            product = connection.getProductById(productId);
            message = "Точно уверен, что хочешь удалить товар\n" + product.fullURL() + " ?";
            InlineKeyboardMarkup confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmproductdeletion_" + productId, "✅ Подтвердить",
                                                    "/cancelproductdeletion_" + productId, "❌ Отменить"),
                                                    2, InlineKeyboardMarkup.class);
    
            userStates.put(chatId, BotState.CONFIRMING_PRODUCT_DELETION);
            sender.editMessage(chatId, messageId, message, confirmationKeyboard, false);
        }
    }

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
    }
}

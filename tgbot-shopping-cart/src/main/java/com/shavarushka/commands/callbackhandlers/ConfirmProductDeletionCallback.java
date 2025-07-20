package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifierCallback;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class ConfirmProductDeletionCallback extends SelectedCartNotifierCallback {
    private final SQLiteConnection connection;

    public ConfirmProductDeletionCallback(MessageSender sender, Map<Long, BotState> userStates,
                                        List<CartSelectionListener> listeners, SQLiteConnection connection) {
        super(sender, userStates, listeners);
        this.connection = connection;
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_PRODUCT_DELETION);
    }
    
    @Override
    public String getCommand() {
        return "/confirmproductdeletion_"; // + productId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long productId = Long.parseLong(update.getCallbackQuery().getData().substring(getCommand().length()));
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Users user = connection.getUserById(userId);
        String message;
        
        if (isProductExist(productId)) {
            boolean isProductLastInTheCategory = isProductLastInTheCategory(productId);
            connection.deleteProduct(productId);
            message = "✅ Товар удален😎";
            sender.editMessage(chatId, messageId, message, false);
            if (isProductLastInTheCategory) {
                notifyCartSelectionListeners(userId, user.selectedCartId());
            }
        }
        userStates.remove(chatId);
    }

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
    }

    private boolean isProductLastInTheCategory(Long productId) {
        return connection.getProductsByCategoryId(connection.getProductById(productId).assignedCategoryId()).size() == 1;
    }
}

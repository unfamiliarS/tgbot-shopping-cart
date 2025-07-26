package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.SettingsDependantNotifier;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class ConfirmProductDeletionCallback extends AbstractCallbackCommand {
    public ConfirmProductDeletionCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
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
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Users user = connection.getUserById(userId);
        Products product = connection.getProductById(productId);
        String message;

        if (isProductExist(productId)) {
            connection.deleteProduct(productId);
        }
        sender.deleteMessage(chatId, messageId);
        userStates.remove(chatId);

        message = "удалил(а) товар\n" + product.fullURL();
        notifyAllIfEnabled(user.userId(), user.selectedCartId(), SettingsDependantNotifier.NotificationType.PRODUCT_DELETED, message);
    }

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
    }
}

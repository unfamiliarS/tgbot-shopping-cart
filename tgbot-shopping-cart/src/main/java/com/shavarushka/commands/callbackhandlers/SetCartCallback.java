package com.shavarushka.commands.callbackhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;

public class SetCartCallback extends AbstractCallbackCommand {

    public SetCartCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/setcart_";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;

        if (!checkForUserExisting(chatId, userId) || !checkForAnyAssignedCartsExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long newSelectedCartId = extractIdFromMessage(update.getCallbackQuery().getData());
        
        if (!isCartExists(newSelectedCartId)) {
            // skip
        } else if (isThisCartAlreadySelected(userId, newSelectedCartId)) {
            // skip
        } else if (!isThisCartAssignedToUser(newSelectedCartId, userId)) {
            // skip
        } else {
            connection.updateSelectedCartForUser(userId, newSelectedCartId);
            updateReplyKeyboard(userId, newSelectedCartId);
        }

        newSelectedCartId = connection.getUserById(userId).selectedCartId();
        message = "Ваши корзины:";
        var keyboard = getMyCartsKeyboard(connection.getCartsAssignedToUser(userId), newSelectedCartId);
        
        sender.editMessage(chatId, messageId, message, keyboard, false);
    }

    private boolean isThisCartAssignedToUser(Long cartId, Long userId) {
        Set<ShoppingCarts> userCarts = connection.getCartsAssignedToUser(userId);
        for (ShoppingCarts cart : userCarts) {
            if (cart.cartId().equals(cartId)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThisCartAlreadySelected(Long userId, Long newSelectedCartId) {
        Long selectedCartId = connection.getUserById(userId).selectedCartId();
        return selectedCartId != null && selectedCartId.equals(newSelectedCartId);
    }

}

package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractConfirmCallback;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmCartCreationCallback extends AbstractConfirmCallback {
    private final Map<Long, String> cartNames;

    public ConfirmCartCreationCallback(MessageSender sender, Map<Long, BotState> userStates,
                                    SQLiteConnection connection, Map<Long, String> cartNames) {
        super(sender, userStates, connection);
        this.cartNames = cartNames;
    }

    @Override
    public String getCommand() {
        return "/confirmcartcreation";
    }

    @Override
    public boolean shouldProcess(Update update) {
        return shouldProcessConfirming(this, update, BotState.CONFIRMING_CART_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String cartName = cartNames.remove(chatId);
        Users user = connection.getUserById(userId);
        
        if (cartName != null) {
            // create shopping cart
            ShoppingCarts cart = new ShoppingCarts(cartName);
            connection.addCart(cart, user.userId());
    
            user = connection.getUserById(user.userId());            
            
            userStates.remove(chatId);
            
            sender.deleteMessage(chatId, messageId);
            
            updateReplyKeyboard(user.userId(), user.selectedCartId());
        }
    }
}

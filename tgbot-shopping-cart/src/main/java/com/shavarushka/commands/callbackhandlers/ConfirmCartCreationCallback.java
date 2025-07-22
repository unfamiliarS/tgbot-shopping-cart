package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifierCallback;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmCartCreationCallback extends SelectedCartNotifierCallback {
    private final Map<Long, String> cartNames;

    public ConfirmCartCreationCallback(MessageSender sender, Map<Long, BotState> userStates,
                                    SQLiteConnection connection, Map<Long, String> cartNames,
                                    List<CartSelectionListener> listeners) {
        super(sender, userStates, connection, listeners);
        this.cartNames = cartNames;
    }

    @Override
    public String getCommand() {
        return "/confirmcartcreation";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION);
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
            ShoppingCarts cart = new ShoppingCarts(null, cartName, null);
            connection.addCart(cart, user.userId());
    
            // notify to update keyboard on new selected cart
            user = connection.getUserById(user.userId());            
            notifyCartSelectionListeners(user.userId(), user.selectedCartId());
            
            userStates.remove(chatId);
            sender.deleteMessage(chatId, messageId);
        }
    }
}

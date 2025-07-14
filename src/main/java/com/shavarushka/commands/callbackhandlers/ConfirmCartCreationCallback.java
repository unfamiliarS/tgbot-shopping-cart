package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmCartCreationCallback extends SelectedCartNotifier {
    private final SQLiteConnection connection;
    private final Map<Long, String> cartNames;

    public ConfirmCartCreationCallback(MessageSender sender, Map<Long, BotState> userStates,
                                    SQLiteConnection connection, Map<Long, String> cartNames,
                                    List<CartSelectionListener> listeners) {
        super(sender, userStates, listeners);
        this.connection = connection;
        this.cartNames = cartNames;
    }

    @Override
    public String getCallbackPattern() {
        return "/confirmcartcreation";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String cartName = cartNames.get(chatId);
        cartNames.remove(chatId);
        
        // create shopping cart
        ShoppingCarts cart = new ShoppingCarts(null, cartName, null);
        
        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        // register new user if needed
        if (user == null) {
            user = new Users(update.getCallbackQuery().getFrom().getId(),
                            chatId,
                            update.getCallbackQuery().getFrom().getFirstName(),
                            update.getCallbackQuery().getFrom().getUserName(),
                            null, // adding later
                            null); // db will figure it out itself
            connection.addUser(user);
        }
        connection.addCart(cart, user);

        // notify to update keyboard on new selected cart
        user = connection.getUserById(user.userId());
        notifyCartSelectionListeners(user.userId(), user.selectedCartId());
        
        userStates.remove(chatId);
        String message = "✅ Корзина *" + MessageSender.escapeMarkdownV2(cartName) + "* создана";
        sender.editMessage(chatId, messageId, message, true);

    }
}

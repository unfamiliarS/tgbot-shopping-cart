package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmCartCreationCallback extends AbstractCallbackCommand {
    private final SQLiteConnection connection;

    public ConfirmCartCreationCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCallbackPattern() {
        return "/confirmcartcreation_"; // + cartName
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String cartName = update.getCallbackQuery().getData().substring(getCallbackPattern().length());

        // create shopping cart
        ShoppingCarts cart = new ShoppingCarts(null, cartName, null);

        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        connection.addCart(cart, user);

        String message = "✅ " + MessageSender.escapeMarkdownV2("Корзина ") + "*" + cartName + "*" + " создана";
        userStates.remove(chatId);
        sender.editMessage(chatId, messageId, message);
    }
}

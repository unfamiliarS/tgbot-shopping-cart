package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class ConfirmCartDeletion extends AbstractCallbackCommand {
    private final SQLiteConnection connection;

    public ConfirmCartDeletion(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCallbackPattern() {
        return "/confirmcartdeletion_"; // + cartId
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long cartForDeletionId = Long.parseLong(update.getCallbackQuery().getData().substring(getCallbackPattern().length()));
        String cartName = connection.getCartById(cartForDeletionId).cartName();
        String message;

        if (connection.getUsersAssignedToCart(cartForDeletionId).size() <= 1) {
            connection.deleteCart(cartForDeletionId);
        }
        connection.deleteCartFromIntermediate(userId, cartForDeletionId);

        userStates.remove(chatId);
        message = "✅ Корзина *" + MessageSender.escapeMarkdownV2(cartName) + "* удалена😎";
        sender.editMessage(chatId, messageId, message, true);
    }
}

package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmInvitingCallback extends AbstractCallbackCommand {
    public ConfirmInvitingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/confirminviting_";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long cartId = extractIdFromMessage(update.getCallbackQuery().getData());

        if (!isCartExist(cartId)) {
            message = "Такой корзины несуществует...😔 Возможно она была удалена🤔";
            sender.editMessage(chatId, messageId, message, false);
        }

        if (isUserAlreadyHaveThisCart(userId, cartId)) {
            message = "Вы уже состоите в этой корзине😋";
            sender.editMessage(chatId, messageId, message, false);
        }

        connection.addUserToCartIntermediate(userId, cartId);
        connection.updateSelectedCartForUser(userId, cartId);
        updateReplyKeyboardOnDataChanges(userId, cartId);

        notifyUsersForInviteConfirmation(update, cartId);

        message = "✅ Приглашение принято\\. Добро пожаловать в " 
                        + MessageSender.escapeMarkdownV2(connection.getCartById(cartId).cartName() + "!");
        sender.editMessage(chatId, messageId, message, true);
    }

    private void notifyUsersForInviteConfirmation(Update update, Long cartId) throws TelegramApiException {
        Set<Users> users;
        if ((users = connection.getUsersAssignedToCart(cartId)).isEmpty())
            return;
        for (Users user : users) {
            if (user.userId().equals(update.getCallbackQuery().getFrom().getId()))
                continue;
            String name = update.getCallbackQuery().getFrom().getUserName();
            if (name.isEmpty() || name == null)
                name = update.getCallbackQuery().getFrom().getFirstName();
            sender.sendMessage(user.chatId(), "@" +  name + " вступил в корзину!", false);
        }
    }

    private boolean isCartExist(Long cartId) {
        return connection.getCartById(cartId) == null ? false : true;
    }

    private boolean isUserAlreadyHaveThisCart(Long userId, Long cartId) {
        Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);
        for (ShoppingCarts cart : carts) {
            if (cart.cartId() == cartId) {
                return true;
            }
        }
        return false;
    }
}

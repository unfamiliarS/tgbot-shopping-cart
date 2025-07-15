package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class ConfirmInvitingCallback extends SelectedCartNotifier {
    private final SQLiteConnection connection;

    public ConfirmInvitingCallback(MessageSender sender, Map<Long, BotState> userStates,
                                SQLiteConnection connection, List<CartSelectionListener> listeners) {
        super(sender, userStates, listeners);
        this.connection = connection;
    }

    @Override
    public String getCommand() {
        return "/confirminviting_";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long cartId = Long.parseLong(update.getCallbackQuery().getData().substring(getCommand().length()));
        String message;

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
        // notify to update keyboard on new selected cart
        notifyCartSelectionListeners(userId, cartId);

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

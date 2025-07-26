package com.shavarushka.commands.commandhandlers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;

public class MyCartCommand extends AbstractTextCommand {
    public MyCartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/mycarts";
    }

    @Override
    public String getDescription() {
        return "Настройка твоих корзин";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
        
        if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
            return;
        
        Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);
        ShoppingCarts selectedCart = connection.getCartById(
                                    connection.getUserById(userId).selectedCartId());

        message = "Ваши корзины:";
        InlineKeyboardMarkup keyboard = getKeyboardForMyCart(carts, selectedCart.cartId());
        sender.sendMessage(chatId, message, keyboard, false);
    }

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

            if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
                return;

            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long newSelectedCartId = extractIdFromMessage(update.getCallbackQuery().getData());
            
            if (connection.getCartById(newSelectedCartId) == null) {
                // skip if cart doesn't exist
            } else if (isThisCartAlreadySelected(userId, newSelectedCartId)) {
                // skip
            } else if (!isThisCartAssignedToUser(newSelectedCartId, userId)) {
                // skip
            } else {
                connection.updateSelectedCartForUser(userId, newSelectedCartId);
                updateReplyKeyboardOnDataChanges(userId, newSelectedCartId);
            }

            newSelectedCartId = connection.getUserById(userId).selectedCartId();
            message = "Ваши корзины:";
            InlineKeyboardMarkup keyboard = getKeyboardForMyCart(connection.getCartsAssignedToUser(userId), newSelectedCartId);
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

    private InlineKeyboardMarkup getKeyboardForMyCart(Set<ShoppingCarts> carts, Long selectedCartId) {
        Map<String, String> buttons = new LinkedHashMap<>();
        carts.stream()
            .sorted(Comparator.comparing(ShoppingCarts::creationTime))
            .forEach(cart -> {
                String cartName = cart.cartId().equals(selectedCartId) 
                    ? "✅ " + cart.cartName() 
                    : cart.cartName();
                buttons.put("/setcart_" + cart.cartId(), cartName);
                buttons.put("/deletecart_" + cart.cartId(), "🗑");
            });
            buttons.put("/close", "✖ Закрыть");
        return KeyboardsFabrics.createKeyboard(buttons,2, InlineKeyboardMarkup.class);
    }
}

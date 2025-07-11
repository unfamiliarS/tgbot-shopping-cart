package com.shavarushka.commands.commandhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;

public class MyCartCommand extends AbstractTextCommand {
    private final SQLiteConnection connection;

    public MyCartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
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
        
        // check if user's carts empty
        Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);
        if (carts.isEmpty()) {
            message = "У вас нет ни одной корзины:( \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return;
        }
        
        ShoppingCarts selectedCart = connection.getCartById(
                                    connection.getUserById(userId).selectedCartId());

        message = "Ваши корзины:";
        Map<String, String> buttons = new HashMap<>();
        for (ShoppingCarts cart : carts) {
            String cartName = cart.cartId().equals(selectedCart.cartId()) ? "✅ " + cart.cartName() : cart.cartName();
            buttons.put(cartName, "/setcart_" + cart.cartId());
        }
        InlineKeyboardMarkup keyboard = KeyboardsFabrics.createInlineKeyboard(
                        buttons,
                        1);
        sender.sendMessage(chatId, message, keyboard, false);
    }

    public class SetCartCallback extends AbstractCallbackCommand {

        public SetCartCallback(MessageSender sender, Map<Long, BotState> userStates) {
            super(sender, userStates);
        }

        @Override
        public String getCallbackPattern() {
            return "/setcart_";
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long userId = update.getCallbackQuery().getFrom().getId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long newSelectedCartId = Long.parseLong(update.getCallbackQuery().getData().substring(getCallbackPattern().length()));
            String message;
            Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);

            if (carts.isEmpty()) {
                message = "У вас нет ни одной корзины:( \n/createnewcart чтобы создать";
                sender.sendMessage(chatId, message, false);
                return;
            }

            connection.updateSelectedCartForUser(userId, newSelectedCartId);

            message = "Ваши корзины:";
            Map<String, String> buttons = new HashMap<>();
            for (ShoppingCarts cart : carts) {
                String cartName = cart.cartId().equals(newSelectedCartId) ? "✅ " + cart.cartName() : cart.cartName();
                buttons.put(cartName, "/setcart_" + cart.cartId());
            }
            InlineKeyboardMarkup keyboard = KeyboardsFabrics.createInlineKeyboard(
                            buttons,
                            1);
            sender.editMessage(chatId, messageId, message, keyboard, false);
        }
    }
}

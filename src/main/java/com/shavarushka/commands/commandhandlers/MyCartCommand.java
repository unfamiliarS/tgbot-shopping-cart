package com.shavarushka.commands.commandhandlers;

import java.util.Comparator;
import java.util.LinkedHashMap;
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
        
        Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);
        
        // check if user's carts empty
        if (carts.isEmpty()) {
            message = "У вас нет ни одной корзины:( \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return;
        }
        
        ShoppingCarts selectedCart = connection.getCartById(
                                    connection.getUserById(userId).selectedCartId());

        message = "Ваши корзины:";
        InlineKeyboardMarkup keyboard = getKeyboardForMyCart(carts, selectedCart.cartId());
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
            InlineKeyboardMarkup keyboard = getKeyboardForMyCart(carts, newSelectedCartId);
            sender.editMessage(chatId, messageId, message, keyboard, false);
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
        return KeyboardsFabrics.createInlineKeyboard(buttons,2);
    }
}

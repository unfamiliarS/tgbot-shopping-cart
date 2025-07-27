package com.shavarushka.commands.commandhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;

public class MyCartsCommand extends AbstractTextCommand {

    public MyCartsCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/mycarts";
    }

    @Override
    public String getDescription() {
        return "Список твоих корзин";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
        
        if (!checkForUserExisting(chatId, userId) || !checkForAnyAssignedCartsExisting(chatId, userId))
            return;
        
        Set<ShoppingCarts> assignedCarts = connection.getCartsAssignedToUser(userId);
        ShoppingCarts selectedCart = connection.getCartById(
                                     connection.getUserById(userId).selectedCartId());

        message = "Ваши корзины:";
        var keyboard = getMyCartsKeyboard(assignedCarts, selectedCart.cartId());
        
        sender.sendMessage(chatId, message, keyboard, false);
    }

}

package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class DeleteCartCallback extends AbstractCallbackCommand {
    private final SQLiteConnection connection;

    public DeleteCartCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCallbackPattern() {
        return "/deletecart_"; // + cardId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long cartForDeletionId = Long.parseLong(update.getCallbackQuery().getData().substring(getCallbackPattern().length()));
        ShoppingCarts cart = connection.getCartById(cartForDeletionId);
        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        String message;
        
        if (user.selectedCartId().equals(cartForDeletionId)) {
            message = "*" + cart.cartName() + "* сейчас выбранна\\, не могу её удалить🤔";
            sender.sendMessage(chatId, message, true);
            return;
        }
        
        message = "Точно уверен\\, что хочешь удалить корзину *" + MessageSender.escapeMarkdownV2(cart.cartName()) + "*\\?";
        if (connection.getUsersAssignedToCart(cartForDeletionId).size() <= 1) {
            message += MessageSender.escapeMarkdownV2("\nТы единственный связанный с ней человек, так что после удаления, её и связанные с ней данные невозможно будет вернуть.")
                    + "\nПодумай хорошенько🤔";
        }

        ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createInlineKeyboard(
                                        Map.of("/confirmcartdeletion_" + cartForDeletionId, "✅ Подтвердить",
                                                "/cancelcartdeletion", "❌ Отменить"),
                                                2);

        userStates.put(chatId, BotState.CONFIRMING_CART_DELETION);
        sender.sendMessage(chatId, message, confirmationKeyboard, true);
    }

}

package com.shavarushka.commands.callbackhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class DeleteCartCallback extends AbstractCallbackCommand {
    public DeleteCartCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/deletecart_"; // + cardId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long cartForDeletionId = extractIdFromMessage(update.getCallbackQuery().getData());
        ShoppingCarts cart = connection.getCartById(cartForDeletionId);
        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        
        if (connection.getCartById(cartForDeletionId) == null) {
            // skip if cart doesn't exist
        } else if (!isThisCartAssignedToUser(cartForDeletionId, user.userId())) {
            // skip if this cart isn't assigned to user
        } else if (user.selectedCartId().equals(cartForDeletionId)) {
            message = "*" + cart.cartName() + "* сейчас выбранна\\, не могу её удалить🤔";
            sender.sendMessage(chatId, message, true);
        } else {
            message = "Точно уверен\\, что хочешь удалить корзину *" + MessageSender.escapeMarkdownV2(cart.cartName()) + "*\\?";
            if (connection.getUsersAssignedToCart(cartForDeletionId).size() <= 1) {
                message += MessageSender.escapeMarkdownV2("\n\nТы единственный связанный с ней человек, так что после удаления, её и связанные с ней данные невозможно будет вернуть.")
                        + "\nПодумай хорошенько🤔";
            }
    
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmcartdeletion_" + cartForDeletionId, "✅ Подтвердить",
                                                    "/cancelcartdeletion", "❌ Отменить"),
                                                    2, InlineKeyboardMarkup.class);
    
            userStates.put(chatId, BotState.CONFIRMING_CART_DELETION);
            sender.sendMessage(chatId, message, confirmationKeyboard, true);
            sender.deleteMessage(chatId, messageId);
        }
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
}

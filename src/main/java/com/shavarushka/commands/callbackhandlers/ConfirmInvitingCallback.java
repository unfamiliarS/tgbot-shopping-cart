package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class ConfirmInvitingCallback extends AbstractCallbackCommand {
    private final SQLiteConnection connection;

    public ConfirmInvitingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCallbackPattern() {
        return "/confirminviting_";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long cartId = Long.parseLong(update.getCallbackQuery().getData().substring(getCallbackPattern().length()));

        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        connection.addUserToCart(user, cartId);
        connection.updateSelectedCartForUser(user.userId(), cartId);

        String message = "✅ Приглашение принято\\. Добро пожаловать в " 
                        + MessageSender.escapeMarkdownV2(connection.getCartById(cartId).cartName()) + "\\!";
        sender.editMessage(chatId, messageId, message);
    }

}

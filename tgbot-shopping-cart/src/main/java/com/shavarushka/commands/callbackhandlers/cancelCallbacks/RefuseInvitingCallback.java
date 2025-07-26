package com.shavarushka.commands.callbackhandlers.cancelCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCancelCallback;
import com.shavarushka.commands.interfaces.SettingsDependantNotifier;
import com.shavarushka.database.SQLiteConnection;

public class RefuseInvitingCallback extends AbstractCancelCallback {

    public RefuseInvitingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/refuseinviting_"; // + userId who initial invite
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long userNotifierId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (!checkForUserExisting(chatId, userNotifierId))
            return;

        String message = "❌ Отменяю вступление в корзину...";
        processCanceling(update, message);

        Long userId = extractIdFromMessage(update.getCallbackQuery().getData());
        processNotification(userNotifierId, userId);
    }
    
    private void processNotification(Long userNotifierId, Long userId) throws TelegramApiException {
        String message = "отклонил(а) заявку на вступление в корзину";
        notifyIfEnabled(userNotifierId, userId, message, SettingsDependantNotifier.NotificationType.REFUSING_OF_JOINING_THE_CART);
    }
}

package com.shavarushka.commands.callbackhandlers.settingCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Settings;

public class SettingListAlreadyPurchasedCallback extends AbstractCallbackCommand {

    public SettingListAlreadyPurchasedCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/listalreadypurchased";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Settings setting = connection.getSettingsById(userId);

        if (setting != null && setting.listAlreadyPurchased() != null) {
            connection.updateListAlreadyPurchasedSetting(userId, !setting.listAlreadyPurchased());
        }
        setting = connection.getSettingsById(userId);
        message = "⚙️ Твои настройки";
        var keyboard = getSettingsKeyboard(setting);
        sender.editMessage(chatId, messageId, message, keyboard, false);
    }
}

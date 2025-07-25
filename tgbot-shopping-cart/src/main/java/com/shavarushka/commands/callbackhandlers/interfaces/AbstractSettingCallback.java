package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Settings;

public abstract class AbstractSettingCallback extends AbstractCallbackCommand {
    public AbstractSettingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    protected void processSettingChange(Update update, BotCommand callback) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
        Settings setting;
    
        if (!checkForUserExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        setting = connection.getSettingsById(userId);

        if (setting != null) {
            switch (callback.getCommand()) {
                case "/listalreadypurchased" 
                    -> processListAlreadyPurchasedSetting(userId, !setting.listAlreadyPurchased());
                case "/notifyaboutproducts"
                    -> processNotifyAboutProductsSetting(userId, !setting.notifyAboutProducts());
                case "/notifyaboutinviting"
                    -> processNotifyAboutInvitingSetting(userId, !setting.notifyAboutInviting());
            }
        }

        setting = connection.getSettingsById(userId);
        message = "⚙️ Твои настройки";
        var keyboard = getSettingsKeyboard(setting);
        sender.editMessage(chatId, messageId, message, keyboard, false);
    }

    private void processListAlreadyPurchasedSetting(Long userId, Boolean newSettingVal) {
        connection.updateListAlreadyPurchasedSetting(userId, newSettingVal);
    }

    private void processNotifyAboutProductsSetting(Long userId, Boolean newSettingVal) {
        connection.updateNotifyAboutProductsSetting(userId, newSettingVal);
    }

    private void processNotifyAboutInvitingSetting(Long userId, Boolean newSettingVal) {
        connection.updateNotifyAboutInvitingSetting(userId, newSettingVal);
    }
}

package com.shavarushka.commands.callbackhandlers.settingCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractSettingCallback;
import com.shavarushka.database.SQLiteConnection;

public class SettingListAlreadyPurchasedCallback extends AbstractSettingCallback {

    public SettingListAlreadyPurchasedCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/listalreadypurchased";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        processSettingChange(update, this);
    } 
}

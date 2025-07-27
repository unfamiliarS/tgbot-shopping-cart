package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.database.SQLiteConnection;

public class SettingsCommand extends AbstractTextCommand {

    public SettingsCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/settings";
    }

    @Override
    public String getDescription() {
        return "Управление настройками бота";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId))
            return;   

        var settings = connection.getSettingsById(userId);

        message = "⚙️ Твои настройки";
        var keyboard = getSettingsKeyboard(settings);
        
        sender.sendMessage(chatId, message, keyboard, false);
    }
}

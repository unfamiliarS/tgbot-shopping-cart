package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;

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
        
    }

}

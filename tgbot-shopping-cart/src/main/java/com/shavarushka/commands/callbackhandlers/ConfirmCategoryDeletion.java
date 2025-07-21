package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifierCallback;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;

public class ConfirmCategoryDeletion extends SelectedCartNotifierCallback {
    public ConfirmCategoryDeletion(MessageSender sender, Map<Long, BotState> userStates,
                    SQLiteConnection connection, List<CartSelectionListener> listeners) {
        super(sender, userStates, connection, listeners);
    }

    @Override
    public String getCommand() {
        return "/confirmcategorydeletion_";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        
    }
}

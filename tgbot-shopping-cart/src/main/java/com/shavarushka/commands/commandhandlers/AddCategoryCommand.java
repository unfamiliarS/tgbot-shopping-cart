package com.shavarushka.commands.commandhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.commandhandlers.interfaces.SelectedCartNotifierCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;

public class AddCategoryCommand extends SelectedCartNotifierCommand {
    public AddCategoryCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection,
                            List<CartSelectionListener> listeners) {
        super(sender, userStates, connection, listeners);
    }

    @Override
    public String getCommand() {
        return "Создать категорию";
    }

    @Override
    public String getDescription() {
        return getCommand();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        
    }
}

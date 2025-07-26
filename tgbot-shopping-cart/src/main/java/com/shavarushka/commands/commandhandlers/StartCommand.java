package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class StartCommand extends AbstractTextCommand {
    public StartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Приветствие!";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String firstname = update.getMessage().getFrom().getFirstName();

        Users user = connection.getUserById(userId);
        // register new user if needed
        if (user == null) {
            user = new Users(userId,
                            chatId,
                            firstname,
                            update.getMessage().getFrom().getUserName(),
                            null, // adding later
                            null // db will figure it out itself
        );
            connection.addUser(user);
        }
        
        sender.sendMessage(chatId, "Привет *" + firstname + "*\\!", true);
        
        // update chatId if needed
        if (chatId != user.chatId()) {
            // TODO: logic to update chatId
        }

        updateReplyKeyboardOnDataChanges(userId, user.selectedCartId());
    }

}
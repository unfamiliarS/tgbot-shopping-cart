package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class StartCommand extends AbstractTextCommand {
    private final SQLiteConnection connection;

    public StartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
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
                            null); // db will figure it out itself
            connection.addUser(user);
        }
        // update chatId if needed
        if (chatId != user.chatId()) {
            // TODO: logic to update chatId
        }

        sender.sendMessage(chatId, "Привет *" + firstname + "*\\!", true);
    }
}
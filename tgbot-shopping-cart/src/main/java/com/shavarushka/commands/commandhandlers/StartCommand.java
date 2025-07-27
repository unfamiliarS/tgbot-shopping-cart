package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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

        var tgUser = update.getMessage().getFrom();
        var dbUser = connection.getUserById(tgUser.getId());

        dbUser = registerUserIfNeeded(dbUser, tgUser, chatId);
        
        // update user info
        updateChatIdIfNeeded(dbUser, chatId);
        updateFirstNameIfNeeded(dbUser, tgUser);
        updateUserNameIfNeeded(dbUser, tgUser);
        
        sender.sendMessage(chatId, "Привет *" + tgUser.getFirstName() + "*\\!", true);

        updateReplyKeyboard(tgUser.getId(), dbUser.selectedCartId());
    }

    private Users registerUserIfNeeded(Users dbUser, User tgUser, Long chatId) {
        if (dbUser == null) {
            dbUser = new Users(tgUser.getId(), chatId, tgUser.getFirstName(), tgUser.getUserName());
            connection.addUser(dbUser);
        }
        return dbUser;
    }

    private void updateChatIdIfNeeded(Users dbUser, Long chatId) {
        if (chatId != dbUser.chatId()) {
            connection.updateChatIdForUser(dbUser.userId(), chatId);
        }
    }

    private void updateFirstNameIfNeeded(Users dbUser, User tgUser) {
        if (!tgUser.getFirstName().equals(dbUser.firstname())) {
            connection.updateFirstNameForUser(dbUser.userId(), tgUser.getFirstName());
        }
    }

    private void updateUserNameIfNeeded(Users dbUser, User tgUser) {
        if ((tgUser.getUserName() != null && dbUser.username() != null) && !tgUser.getUserName().equals(dbUser.username()) ||
             tgUser.getUserName() != null && dbUser.username() == null ||
             tgUser.getUserName() == null && dbUser.username() != null   
             ) {
            connection.updateUserNameForUser(dbUser.userId(), tgUser.getUserName());
        }
    }
    
}
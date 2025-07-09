package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Users;

public class InviteUserCommand extends AbstractTextCommand {
    private final SQLiteConnection connection;

    public InviteUserCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCommand() {
        return "/inviteuser";
    }

    @Override
    public String getDescription() {
        return "Добавить друга в свою корзину";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
        
        // check if user's carts empty
        if (connection.getCartsAssignedToUser(userId).isEmpty()) {
            message = MessageSender.escapeMarkdownV2(
                "У тебя нет ни одной корзины:( \n/createnewcart чтобы создать");
            sender.sendMessage(chatId, message);
            return;
        }

        sender.sendMessage(chatId, 
                MessageSender.escapeMarkdownV2("Введи @имя_пользователя, которого хочешь пригласить в свою корзину:"),
                KeyboardsFabrics.createInlineKeyboard(Map.of("Отменить ввод", "/cancelinvitinguser"), 1));
        userStates.put(chatId, BotState.WAITING_FOR_USERNAME_TO_INVITE);
    }

    public class UsernameInputHandler extends AbstractTextCommand {
        public UsernameInputHandler(MessageSender sender, Map<Long, BotState> userStates) {
            super(sender, userStates);
        }

        @Override
        public String getCommand() {
            return "invite_username";
        }

        @Override
        public String getDescription() {
            return "";
        }
        
        @Override
        public boolean shouldProcess(Update update) {
            if (!update.hasMessage() || !update.getMessage().hasText())
                return false;

            Long chatId = update.getMessage().getChatId();
            return userStates.containsKey(chatId) &&
                   userStates.get(chatId).equals(BotState.WAITING_FOR_USERNAME_TO_INVITE);
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getMessage().getChatId();
            String usernameToInvite = update.getMessage().getText();
            String message;
            if (!isCorrectUsername(usernameToInvite)) {
                message = MessageSender.escapeMarkdownV2("Некорректное имя пользователя, оно должно начинаться с @.\nПопробуй ещё раз.");
                sender.sendMessage(chatId, message,
                    KeyboardsFabrics.createInlineKeyboard(
                        Map.of("Отменить ввод", "/cancelinvitinguser"),
                        1));
                return;
            }
            
            // check if user exist
            // ...

            Users user = connection.getUserByUsername(usernameToInvite.substring(1));
            if (user == null) {
                System.out.println("User for " + usernameToInvite + " is missing");
                return;
            }

            sender.sendMessage(user.chatId(), "Привет от " + update.getMessage().getFrom().getUserName());

            message = "✅ Приглашение отправленно пользователю *" + usernameToInvite + "*";
            sender.sendMessage(chatId, message);
            userStates.remove(chatId);
        }

        private boolean isCorrectUsername(String username) {
            return username.startsWith("@");  
        }
    }
}

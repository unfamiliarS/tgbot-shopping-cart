package com.shavarushka.commands.commandhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class InviteUserCommand extends AbstractTextCommand {
    public InviteUserCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
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
    
        if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
            return;

        sender.sendMessage(chatId, 
                "Введи @имя_пользователя, которого хочешь пригласить в свою корзину:",
                KeyboardsFabrics.createKeyboard(Map.of("/cancelinvitinguser", "Отменить ввод"), 
                1, InlineKeyboardMarkup.class), false);
        userStates.put(chatId, BotState.WAITING_FOR_USERNAME_TO_INVITE);
    }

    public class UsernameInputHandler extends AbstractTextCommand {
        public UsernameInputHandler(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
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
            Long currentUserId = update.getMessage().getFrom().getId();
            Long invitedCartId = connection.getUserById(currentUserId).selectedCartId();
            String usernameToInvite = update.getMessage().getText();
            String currentUsername = update.getMessage().getFrom().getUserName().isEmpty() ?
                                    update.getMessage().getFrom().getFirstName() :
                                    update.getMessage().getFrom().getUserName();
            String message;

            if (!isCorrectUsername(usernameToInvite)) {
                message = "Некорректное имя пользователя.\nПопробуй ещё раз.";
                sender.sendMessage(chatId, message,
                    KeyboardsFabrics.createKeyboard(
                        Map.of("/cancelinvitinguser", "Отменить ввод"),
                        1, InlineKeyboardMarkup.class), false);
                return;
            }

            if (isItMe(currentUsername, usernameToInvite.substring(1))) {
                message = "Хулиганишь🙃";
                sender.sendMessage(chatId, message, false);
                userStates.remove(chatId);
                return;
            }

            Users invitedUser = connection.getUserByUsername(usernameToInvite.substring(1));
            if (invitedUser == null) {
                message = "Не могу найти пользователя в своей базе😔 Отменяю приглашение...";
                sender.sendMessage(chatId, message, false);
                userStates.remove(chatId);
                return;
            }

            if (isUserAlreadyHaveThisCart(invitedUser.userId(), invitedCartId)) {
                message = usernameToInvite + " уже состоит в этой корзине😋";
                sender.sendMessage(chatId, message, false);
                userStates.remove(chatId);
                return;
            }
            
            inviteUser(currentUsername, currentUserId, invitedUser.chatId(), invitedCartId);

            message = "✅ Приглашение отправленно пользователю " + usernameToInvite;
            sender.sendMessage(chatId, message, false);
            userStates.remove(chatId);
        }

        private boolean isCorrectUsername(String username) {
            return username.toLowerCase().matches("@[a-z0-9_]+");  
        }

        private boolean isItMe(String myUsername, String usernameToInvite) {
            return myUsername.equals(usernameToInvite);  
        }

        private boolean isUserAlreadyHaveThisCart(Long userId, Long cartId) {
            Set<ShoppingCarts> carts = connection.getCartsAssignedToUser(userId);
            for (ShoppingCarts cart : carts) {
                if (cart.cartId() == cartId) {
                    return true;
                }
            }
            return false;
        }

        private void inviteUser(String currentUsername, Long currentUserId, Long invitedChatId, Long invitedCartId) throws TelegramApiException {
            String invitedCart = connection.getCartById(invitedCartId).cartName();
            String invitingMessage = "@" + MessageSender.escapeMarkdownV2(currentUsername) +
                                    " приглашает в корзину *" + MessageSender.escapeMarkdownV2(invitedCart) + "*";
            InlineKeyboardMarkup keyboard = KeyboardsFabrics.createKeyboard(
                                Map.of("/confirminviting_" + invitedCartId, "✅ Вступить"
                                    ), 1, InlineKeyboardMarkup.class);
            sender.sendMessage(invitedChatId, invitingMessage, keyboard, true);
        }
        
    }
}

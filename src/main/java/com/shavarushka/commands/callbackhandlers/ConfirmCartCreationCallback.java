package com.shavarushka.commands.callbackhandlers;

import java.util.Map;

import org.hibernate.Session;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.database.DatabaseOperations;
import com.shavarushka.database.entities.ShoppingCart;
import com.shavarushka.database.entities.User;

public class ConfirmCartCreationCallback extends AbstractCallbackCommand {
    DatabaseOperations connection;

    public ConfirmCartCreationCallback(TelegramClient telegramClient, Map<Long, BotState> userStates, DatabaseOperations connection) {
        super(telegramClient, userStates);
        this.connection = connection;
    }

    @Override
    public String getCallbackPattern() {
        return "/confirmcartcreation_"; // + cartName
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String cartName = update.getCallbackQuery().getData().substring(getCallbackPattern().length());

        // boolean isNewUser = false;
        // User user = connection.getUserById(chatId);
        // // adding new user if needed
        // if (user == null) {
        //     user = new User(update.getCallbackQuery().getFrom().getId(),
        //                         update.getCallbackQuery().getFrom().getUserName());
        //     isNewUser = true;
        // }
        // // create shopping cart
        // ShoppingCart cart = new ShoppingCart(cartName, user);
        // // setting this cart for new user
        // if (isNewUser) {
        //     user.setSelectedCart(cart);
        //     connection.addUser(user);
        // }
        
        // connection.addShoppingCart(cart);

        try (Session session = connection.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();

            boolean isNewUser = false;
            User user = session.find(User.class, update.getCallbackQuery().getFrom().getId());
            // adding new user if needed
            if (user == null) {
                user = new User(update.getCallbackQuery().getFrom().getId(),
                                    update.getCallbackQuery().getFrom().getUserName());
                isNewUser = true;
            }
            // create shopping cart
            ShoppingCart cart = new ShoppingCart(cartName, user);
            if (isNewUser) {
                user.setSelectedCart(cart);
                session.persist(user);
            }
            session.persist(cart);

            transaction.commit();
        }

        String message = escapeMarkdownV2("Успешно! Добро пожаловать в ") + "*" + cartName + "*";
        userStates.remove(chatId);
        editMessage(chatId, messageId, message);
    }
}

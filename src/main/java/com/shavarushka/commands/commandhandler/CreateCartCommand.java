package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.KeyboardsFabrics;
import com.shavarushka.commands.intr.BotState;

public class CreateCartCommand extends AbstractTextCommand {
    Map<Long, BotState> userStates;

    public CreateCartCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient);
        this.userStates = userStates;
    }

    @Override
    public String getCommand() {
        return "/createnewcart";
    }

    @Override
    public String getDescription() {
        return "Создание новой корзины";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, 
                "Введите название корзины",
                KeyboardsFabrics.createInlineKeyboard(Map.of("Отменить создание", "/cancelcreatingnewcart"), 1));
        userStates.put(chatId, BotState.WAITING_FOR_CART_NAME);
    }

    public class NameInputHandler extends AbstractTextCommand {
        public NameInputHandler(TelegramClient telegramClient) {
            super(telegramClient);
        }

        @Override
        public String getCommand() {
            return "";
        }

        @Override
        public String getDescription() {
            return "";
        }
        
        @Override
        public boolean shouldProcess(Update update) {
            long chatId = update.getMessage().getChatId();
            return userStates.containsKey(chatId) && 
                   userStates.get(chatId).equals(BotState.WAITING_FOR_CART_NAME);
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            long chatId = update.getMessage().getChatId();
            String cartName = update.getMessage().getText();
            String message = "Вы точно уверены\\, что хотите создать *" + cartName + "*\\?";
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createInlineKeyboard(
                                            Map.of("✅ Подтвердить", "/confirmcartcreation",
                                                   "❌ Отменить", "/cancelcreatingnewcart"),
                                                   2);
            sendMessage(chatId, message, confirmationKeyboard);
            userStates.put(chatId, BotState.CONFIRMING_CART_CREATION);
        }
    }
}

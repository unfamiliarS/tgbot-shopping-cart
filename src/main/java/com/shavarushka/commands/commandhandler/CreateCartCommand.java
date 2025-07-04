package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.intr.BotState;

public class CreateCartCommand extends AbstractTextCommand {
    public CreateCartCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
    }

    @Override
    public String getCommand() {
        return "/createnewcart";
    }

    @Override
    public String getDescription() {
        return """
            Создание новой корзины.
                    - Используйте только буквы, цифры и -_.,!()
                    - Длина от 3 до 30 символов""";
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
        public NameInputHandler(TelegramClient telegramClient, Map<Long, BotState> userStates) {
            super(telegramClient, userStates);
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
            if (!update.hasMessage() || !update.getMessage().hasText())
                return false;

            long chatId = update.getMessage().getChatId();
            return userStates.containsKey(chatId) && 
                   userStates.get(chatId).equals(BotState.WAITING_FOR_CART_NAME);
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            long chatId = update.getMessage().getChatId();
            String cartName = update.getMessage().getText();
            String message;
            if (!isCorrectCartName(cartName)) {
                message = escapeMarkdownV2("Некорректное название для корзины. Попробуй ещё раз.");
                sendMessage(chatId, message,
                    KeyboardsFabrics.createInlineKeyboard(
                        Map.of(escapeMarkdownV2("Отменить создание"),
                        "/cancelcreatingnewcart"),
                        1));
                return;
            }
            message = "Вы точно уверены\\, что хотите создать *" + cartName + "*\\?";
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createInlineKeyboard(
                                            Map.of("✅ Подтвердить", "/confirmcartcreation_" + cartName,
                                                   "❌ Отменить", "/cancelcreatingnewcart"),
                                                   2);
            userStates.put(chatId, BotState.CONFIRMING_CART_CREATION);
            sendMessage(chatId, message, confirmationKeyboard);
        }

        private boolean isCorrectCartName(String cartName) {
            String allowedCharsRegex = "^[a-zA-Zа-яА-ЯёЁ0-9\\s\\-_,.!()]+$";
                    // not null test
            return cartName != null && 
                    // not empty test
                   !cartName.trim().isEmpty() &&
                    // length test
                   (cartName.length() >= 3 && cartName.length() <= 43) &&
                    // test for allowed chars
                    cartName.matches(allowedCharsRegex);           
        }
    }
}
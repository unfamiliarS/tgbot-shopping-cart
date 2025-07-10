package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CreateCartCommand extends AbstractTextCommand {
    private final Map<Long, String> cartNames;

    public CreateCartCommand(MessageSender sender, Map<Long, BotState> userStates, Map<Long, String> cartNames) {
        super(sender, userStates);
        this.cartNames = cartNames;
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
        Long chatId = update.getMessage().getChatId();
        sender.sendMessage(chatId, 
                "Введи название корзины:",
                KeyboardsFabrics.createInlineKeyboard(Map.of("Отменить создание", "/cancelcreatingnewcart"),
                1), false);
        userStates.put(chatId, BotState.WAITING_FOR_CART_NAME);
    }

    public class NameInputHandler extends AbstractTextCommand {
        public NameInputHandler(MessageSender sender, Map<Long, BotState> userStates) {
            super(sender, userStates);
        }

        @Override
        public String getCommand() {
            return "create_cartname";
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
                   userStates.get(chatId).equals(BotState.WAITING_FOR_CART_NAME);
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getMessage().getChatId();
            String cartName = update.getMessage().getText();
            String message;
            
            if (!isCorrectCartName(cartName)) {
                message = "Некорректное название для корзины. Попробуй ещё раз.";
                sender.sendMessage(chatId, message,
                    KeyboardsFabrics.createInlineKeyboard(
                        Map.of("Отменить создание", "/cancelcreatingnewcart"),
                        1), false);
                return;
            }

            cartNames.put(chatId, cartName);
            message = "Вы точно уверены\\, что хотите создать *" + MessageSender.escapeMarkdownV2(cartName) + "* \\?";
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createInlineKeyboard(
                                            Map.of("✅ Подтвердить", "/confirmcartcreation",
                                                   "❌ Отменить", "/cancelcreatingnewcart"),
                                                   2);
            userStates.put(chatId, BotState.CONFIRMING_CART_CREATION);
            sender.sendMessage(chatId, message, confirmationKeyboard, true);
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
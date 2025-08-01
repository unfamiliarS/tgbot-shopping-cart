package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.ShoppingCarts;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

public class CreateCartCommand extends AbstractTextCommand {

    private final Map<Long, String> newCartNames;

    public CreateCartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection, Map<Long, String> newCartNames) {
        super(sender, userStates, connection);
        this.newCartNames = newCartNames;
    }

    @Override
    public String getCommand() {
        return "/createcart";
    }

    @Override
    public String getDescription() {
        return "Разрешены только буквы, цифры, символы -_.,!(), а также некоторые эмодзи";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        
        if (!checkForUserExisting(chatId, userId))
            return;

        sendInitialCreatingCartMessage(chatId);

        userStates.put(chatId, BotState.WAITING_FOR_CART_NAME);
    }

    private void sendInitialCreatingCartMessage(Long chatId) throws TelegramApiException {
        String message = getDescription() + "\n\nВведи название корзины:";
        var keyboard = KeyboardsFabrics.createKeyboard(
            Map.of("/cancelcreatingnewcart", "Отменить создание"), 1, InlineKeyboardMarkup.class
        );
        sender.sendMessage(chatId, message, keyboard, false);
    }

    public class CartNameInputHandler extends AbstractTextCommand {
        
        private NameChecker nameChecker;

        public CartNameInputHandler(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
            nameChecker = new NameChecker();
        }

        @Override
        public String getCommand() {
            return "create_cartname";
        }
        
        @Override
        public boolean shouldProcess(Update update) {
            if (isThisMessage(update)) {
                Long chatId = update.getMessage().getChatId();
                return isUserHaveState(chatId, BotState.WAITING_FOR_CART_NAME);
            }
            return false;
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();
            String cartName = update.getMessage().getText();
            
            if (!checkForUserExisting(chatId, userId)) {
                userStates.remove(chatId);
                return;
            }

            processCreatingConfirmationForCartCreation(chatId, userId, cartName);
        }

        private void processCreatingConfirmationForCartCreation(Long chatId, Long userId, String newCartName) throws TelegramApiException {
            if (isCartWithThatNameAlreadyExist(userId, newCartName)) {
                sendMessageForAlreadyExistingCart(chatId);
            } else if (!nameChecker.isCorrectName(newCartName)) {
                sendMessageForIncorrectCartName(chatId);
            } else {
                newCartNames.put(chatId, newCartName);
                userStates.put(chatId, BotState.CONFIRMING_CART_CREATION);
                sendConfirmationMessage(chatId, newCartName);
            }
        }

        private boolean isCartWithThatNameAlreadyExist(Long userId, String newCartName) {
            for (ShoppingCarts cart : connection.getCartsAssignedToUser(userId)) {
                if (newCartName.equals(cart.cartName()))
                    return true;
            }
            return false;
        }

        private void sendMessageForAlreadyExistingCart(Long chatId) throws TelegramApiException {
            String message = "Корзина с таким названием уже существует 😔 Попробуй ещё раз 🔄";
            sender.sendMessage(chatId, message, false);
        }

        private void sendMessageForIncorrectCartName(Long chatId) throws TelegramApiException {
            String message = "Некорректное название для корзины 😔 Попробуй ещё раз 🔄";
            sender.sendMessage(chatId, message, false);
        }

        private void sendConfirmationMessage(Long chatId, String newCartName) throws TelegramApiException {
            String message = "Вы точно уверены\\, что хотите создать *" + MessageSender.escapeMarkdownV2(newCartName) + "*\\?";
            var confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmcartcreation", "✅ Подтвердить",
                                                    "/cancelcreatingnewcart", "❌ Отменить"),
                                                    2, InlineKeyboardMarkup.class);
            sender.sendMessage(chatId, message, confirmationKeyboard, true);
        }

        private static class NameChecker {

            private final String ALLOWED_CHARS = "a-zA-Zа-яА-ЯёЁ0-9\\s\\-_,.!()";

            private boolean isCorrectName(String name) {
                return !isNameEmpty(name)
                    && isNameHaveCorrectLength(name)
                    && isNameContainsOnlyAllowedChars(name);
            }

            private boolean isNameEmpty(String name) {
                return name == null || name.strip().isEmpty();
            }

            private boolean isNameHaveCorrectLength(String name) {
                return name.length() <= 43;
            }

            private boolean isNameContainsOnlyAllowedChars(String name) {
                boolean nameHaveOnlyAllowedChars = false;
                String allowedCharsRegex = "^[" + ALLOWED_CHARS + "]+$";
                if (EmojiManager.containsEmoji(name) && !isPureEmoji(name)) {
                    name = EmojiParser.removeAllEmojis(name);
                }

                if (name.matches(allowedCharsRegex)) {
                    nameHaveOnlyAllowedChars = true;
                }

                return nameHaveOnlyAllowedChars;
            }

            private boolean isPureEmoji(String str) {
                String textWithoutEmoji = EmojiParser.removeAllEmojis(str);
                return textWithoutEmoji.isEmpty();
            }
        }
    }
}
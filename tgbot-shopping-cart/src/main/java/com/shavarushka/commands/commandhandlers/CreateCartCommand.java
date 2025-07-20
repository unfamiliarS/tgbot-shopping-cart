package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

public class CreateCartCommand extends AbstractTextCommand {
    private final Map<Long, String> cartNames;

    public CreateCartCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection, Map<Long, String> cartNames) {
        super(sender, userStates, connection);
        this.cartNames = cartNames;
    }

    @Override
    public String getCommand() {
        return "/createnewcart";
    }

    @Override
    public String getDescription() {
        return "Разрешены только буквы, цифры, символы -_.,!(), а также некоторые эмодзи";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();

        String message = getDescription() + "\n\nВведи название корзины:";
        InlineKeyboardMarkup keyboard = KeyboardsFabrics.createKeyboard(
            Map.of("/cancelcreatingnewcart", "Отменить создание"), 1, InlineKeyboardMarkup.class
        );
        sender.sendMessage(chatId, message, keyboard, false);
        userStates.put(chatId, BotState.WAITING_FOR_CART_NAME);
    }

    public class NameInputHandler extends AbstractTextCommand {
        public NameInputHandler(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
        }

        @Override
        public String getCommand() {
            return "create_cartname";
        }

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
                    KeyboardsFabrics.createKeyboard(
                        Map.of("/cancelcreatingnewcart", "Отменить создание"),
                        1, InlineKeyboardMarkup.class), false);
                return;
            }

            cartNames.put(chatId, cartName);
            message = "Вы точно уверены\\, что хотите создать *" + MessageSender.escapeMarkdownV2(cartName) + "*\\?";
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmcartcreation", "✅ Подтвердить",
                                                   "/cancelcreatingnewcart", "❌ Отменить"),
                                                   2, InlineKeyboardMarkup.class);
            userStates.put(chatId, BotState.CONFIRMING_CART_CREATION);
            sender.sendMessage(chatId, message, confirmationKeyboard, true);
        }

        private boolean isCorrectCartName(String cartName) {
            // not null and not empty check
            if (cartName == null || cartName.strip().isEmpty()) {
                return false;
            }
            // length check
            if (cartName.length() > 43) {
                return false;
            }

            // check for allowed chars
            if (isPureEmoji(cartName)) {
                return true;
            }
            String allowedCharsRegex = "^[a-zA-Zа-яА-ЯёЁ0-9\\s\\-_,.!()]+$";
            if (EmojiManager.containsEmoji(cartName)) {
                String textWithoutEmoji = EmojiParser.removeAllEmojis(cartName);
                if (textWithoutEmoji.matches(allowedCharsRegex)) {
                    return true;
                }
            }
            if (cartName.matches(allowedCharsRegex)) {
                return true;
            }

            return false;
        }

        private boolean isPureEmoji(String str) {
            String textWithoutEmoji = EmojiParser.removeAllEmojis(str);
            return textWithoutEmoji.isEmpty();
        }
    }
}
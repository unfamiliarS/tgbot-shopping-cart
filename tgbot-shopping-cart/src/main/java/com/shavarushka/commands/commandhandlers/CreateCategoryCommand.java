package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

public class CreateCategoryCommand extends AbstractTextCommand {

    private final Map<Long, String> categoryNames;

    public CreateCategoryCommand(MessageSender sender, Map<Long, BotState> userStates,
                            SQLiteConnection connection, Map<Long, String> categoryNames) {
        super(sender, userStates, connection);
        this.categoryNames = categoryNames;
    }

    @Override
    public String getCommand() {
        return "Создать категорию";
    }

    @Override
    public String getDescription() {
        return "Разрешены только буквы, цифры, символы -_.,!(), а также некоторые эмодзи";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        message = getDescription() + "\n\nВведи название категории:";
        InlineKeyboardMarkup keyboard = KeyboardsFabrics.createKeyboard(
            Map.of("/cancelcreatingcategory", "Отменить создание"), 1, InlineKeyboardMarkup.class
        );
        sender.sendMessage(chatId, message, keyboard, false);
        userStates.put(chatId, BotState.WAITING_FOR_CATEGORY_NAME);
    }

    public class CategoryNameInputHandler extends AbstractTextCommand {
        public CategoryNameInputHandler(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
        }

        @Override
        public String getCommand() {
            return "CategoryNameInputHandler";
        }
        
        @Override
        public boolean shouldProcess(Update update) {
            if (!update.hasMessage() || !update.getMessage().hasText())
                return false;

            Long chatId = update.getMessage().getChatId();
            return userStates.containsKey(chatId) &&
                   userStates.get(chatId).equals(BotState.WAITING_FOR_CATEGORY_NAME);
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();
            Long selectedCartId = connection.getUserById(userId).selectedCartId();
            String categoryName = update.getMessage().getText();
            String message;

            if (isCategoryAlreadyExist(selectedCartId, categoryName)) {
                message = "Категория с таким названием уже существует 😔 Попробуй ещё раз 🔄";
                sender.sendMessage(chatId, message, false);
                return;
            } else if (!isCorrectName(categoryName)) {
                message = "Некорректное название для категории 😔 Попробуй ещё раз 🔄";
                sender.sendMessage(chatId, message, false);
                return;
            }

            categoryNames.put(chatId, categoryName);
            message = "Вы точно уверены\\, что хотите создать *" + MessageSender.escapeMarkdownV2(categoryName) + "*\\?";
            ReplyKeyboard confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmcategorycreation", "✅ Подтвердить",
                                                   "/cancelcreatingcategory", "❌ Отменить"),
                                                   2, InlineKeyboardMarkup.class);
            userStates.put(chatId, BotState.CONFIRMING_CATEGORY_CREATION);
            sender.sendMessage(chatId, message, confirmationKeyboard, true);
        }

        private boolean isCorrectName(String name) {
            // not null and not empty check
            if (name == null || name.strip().isEmpty()) {
                return false;
            }
            // length check
            if (name.length() > 43) {
                return false;
            }

            // check for allowed chars
            if (isPureEmoji(name)) {
                return true;
            }
            String allowedCharsRegex = "^[a-zA-Zа-яА-ЯёЁ0-9\\s\\-_,.!()]+$";
            if (EmojiManager.containsEmoji(name)) {
                String textWithoutEmoji = EmojiParser.removeAllEmojis(name);
                if (textWithoutEmoji.matches(allowedCharsRegex)) {
                    return true;
                }
            }
            if (name.matches(allowedCharsRegex)
                && !name.equals("Удалить категорию")
                && !name.equals("Создать категорию")) {
                return true;
            }

            
            return false;
        }

        private boolean isPureEmoji(String str) {
            String textWithoutEmoji = EmojiParser.removeAllEmojis(str);
            return textWithoutEmoji.isEmpty();
        }

        private boolean isCategoryAlreadyExist(Long currentCartId, String newCategoryName) {
            for (Categories category : connection.getCategoriesByCartId(currentCartId)) {
                if (newCategoryName.equals(category.categoryName()))
                    return true;
            }
            return false;
        }
    } 
}

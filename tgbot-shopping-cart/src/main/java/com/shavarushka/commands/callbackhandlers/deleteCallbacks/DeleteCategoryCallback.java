package com.shavarushka.commands.callbackhandlers.deleteCallbacks;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;

public class DeleteCategoryCallback extends AbstractCallbackCommand {

    public DeleteCategoryCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/deletecategory_"; // + id
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long categoryId = extractIdFromMessage(update.getCallbackQuery().getData());
        Categories category;

        if (!isCategoryExist(categoryId)) {
            message = "Такой категории не существует🤔";
            sender.editMessage(chatId, messageId, message, false);
            return;
        }

        category = connection.getCategoryById(categoryId);
        message = "Уверен что хочешь удалить *" + MessageSender.escapeMarkdownV2(category.categoryName()) + "*?";
        var keyboard = KeyboardsFabrics.createKeyboard(
            Map.of(
                "/confirmcategorydeletion_" + categoryId, "✅ Подтвердить",
                "/cancelcategorydeletion_" + categoryId, "❌ Отменить"
            ),
            2,
            InlineKeyboardMarkup.class
        );

        if (isCategoryContainsProducts(categoryId)) {
            int productCnt = connection.getProductsByCategoryId(categoryId).size();
            message = "Товаров, хранящихся в этой категории: " + productCnt + " 😱\n\n" + message;
            sender.editMessage(chatId, messageId, message, keyboard, true);
        } else {
            sender.editMessage(chatId, messageId, message, keyboard, true);
        }
        
        userStates.put(chatId, BotState.CONFIRMING_CATEGORY_DELETION);
    }

    private boolean isCategoryExist(Long categoryId) {
        return connection.getCategoryById(categoryId) != null;
    }

    private boolean isCategoryContainsProducts(Long categoryId) {
        return !connection.getProductsByCategoryId(categoryId).isEmpty();
    }
}

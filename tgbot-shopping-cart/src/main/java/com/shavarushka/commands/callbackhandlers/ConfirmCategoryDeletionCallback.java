package com.shavarushka.commands.callbackhandlers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifierCallback;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class ConfirmCategoryDeletionCallback extends SelectedCartNotifierCallback {
    public ConfirmCategoryDeletionCallback(MessageSender sender, Map<Long, BotState> userStates,
                    SQLiteConnection connection, List<CartSelectionListener> listeners) {
        super(sender, userStates, connection, listeners);
    }

    @Override
    public String getCommand() {
        return "/confirmcategorydeletion_";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CATEGORY_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Users user = connection.getUserById(userId);
        Long categoryForDeletionId = extractIdFromMessage(update.getCallbackQuery().getData());
        String categoryName = connection.getCategoryById(categoryForDeletionId).categoryName();
        String message;

        if (!isCategoryExist(categoryForDeletionId)) {
            message = "Такой категории не существует🤔";
            sender.editMessage(chatId, messageId, message, false);
            return;
        }

        deleteCategoryAndTheyProducts(categoryForDeletionId);
        userStates.remove(chatId);

        notifyCartSelectionListeners(userId, user.selectedCartId());

        message = "✅ Категория *" + MessageSender.escapeMarkdownV2(categoryName) + "* удалена😎";
        sender.editMessage(chatId, messageId, message, true);
    }

    private boolean isCategoryExist(Long categoryId) {
        return connection.getCategoryById(categoryId) != null;
    }

    private void deleteCategoryAndTheyProducts(Long categoryId) {
        Set<Products> products = connection.getProductsByCategoryId(categoryId);
        for (Products product : products) {
            connection.deleteProduct(product.productId());
        }
        connection.deleteCategory(categoryId);
    }
}

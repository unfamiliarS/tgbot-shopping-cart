package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractConfirmCallback;
import com.shavarushka.commands.interfaces.SettingsDependantNotifier;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class ConfirmCategoryDeletionCallback extends AbstractConfirmCallback {
    public ConfirmCategoryDeletionCallback(MessageSender sender, Map<Long, BotState> userStates,
                    SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/confirmcategorydeletion_";
    }

    @Override
    public boolean shouldProcess(Update update) {
        return shouldProcessConfirming(this, update, BotState.CONFIRMING_CATEGORY_DELETION);
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

        updateReplyKeyboard(userId, user.selectedCartId());

        message = "✅ Категория *" + MessageSender.escapeMarkdownV2(categoryName) + "* удалена😎";
        sender.editMessage(chatId, messageId, message, true);

        message = "удалил(а) категорию '" + categoryName + "'";
        notifyAllIfEnabled(user.userId(), user.selectedCartId(), SettingsDependantNotifier.NotificationType.CATEGORY_DELETED, message);
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

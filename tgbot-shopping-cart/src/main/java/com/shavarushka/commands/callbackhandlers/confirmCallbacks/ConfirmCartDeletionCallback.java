package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractConfirmCallback;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;

public class ConfirmCartDeletionCallback extends AbstractConfirmCallback {
    public ConfirmCartDeletionCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/confirmcartdeletion_"; // + cartId
    }

    @Override
    public boolean shouldProcess(Update update) {
        return shouldProcessConfirming(this, update, BotState.CONFIRMING_CART_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message;
        Long cartForDeletionId = extractIdFromMessage(update.getCallbackQuery().getData());
        String cartName = connection.getCartById(cartForDeletionId).cartName();

        if (connection.getUsersAssignedToCart(cartForDeletionId).size() <= 1) {
            deleteCartTheirCategoriesAndProducts(cartForDeletionId);
        }

        connection.deleteCartFromIntermediate(userId, cartForDeletionId);
        userStates.remove(chatId);

        message = "✅ Корзина *" + MessageSender.escapeMarkdownV2(cartName) + "* удалена😎";
        sender.editMessage(chatId, messageId, message, true);
    }

    private void deleteCartTheirCategoriesAndProducts(Long cartForDeletionId) {
        Set<Categories> categoriesForDeletion = connection.getCategoriesByCartId(cartForDeletionId);
        Set<Products> productsForDeletion;
        if (!categoriesForDeletion.isEmpty()) {
            for (Categories category : categoriesForDeletion) {
                productsForDeletion = connection.getProductsByCategoryId(category.categoryId());
                if (!productsForDeletion.isEmpty()) {
                    for (Products product : productsForDeletion) {
                        connection.deleteProduct(product.productId());
                    }
                }
                connection.deleteCategory(category.categoryId());
            }
        }
        connection.deleteCart(cartForDeletionId);        
    }
}

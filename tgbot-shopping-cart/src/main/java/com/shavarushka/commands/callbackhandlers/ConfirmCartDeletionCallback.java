package com.shavarushka.commands.callbackhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;

public class ConfirmCartDeletionCallback extends AbstractCallbackCommand {
    private final SQLiteConnection connection;

    public ConfirmCartDeletionCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
        this.connection = connection;
    }

    @Override
    public String getCommand() {
        return "/confirmcartdeletion_"; // + cartId
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CART_DELETION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long cartForDeletionId = Long.parseLong(update.getCallbackQuery().getData().substring(getCommand().length()));
        String cartName = connection.getCartById(cartForDeletionId).cartName();
        String message;

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

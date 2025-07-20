package com.shavarushka.commands.callbackhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

public class DeleteProductCallback extends AbstractCallbackCommand {
    public DeleteProductCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "/deleteproduct_"; // + productId
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();
        return !userStates.containsKey(chatId) &&
                callback.startsWith(getCommand().strip());
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Long productId = Long.parseLong(update.getCallbackQuery().getData().substring(getCommand().length()));
        Users user = connection.getUserById(update.getCallbackQuery().getFrom().getId());
        Products product;
        String message;
        
        if (!isProductExist(productId)) {
            sender.deleteMessage(chatId, messageId);
            System.out.println("product don't exist");
        } else if (!isThisProductAssignedToUser(productId, user.userId())) {
            sender.deleteMessage(chatId, messageId);
            System.out.println("product don't assigned by user");
        } else {
            product = connection.getProductById(productId);
            message = "Точно уверен, что хочешь удалить товар\n" + product.fullURL() + " ?";
            InlineKeyboardMarkup confirmationKeyboard = KeyboardsFabrics.createKeyboard(
                                            Map.of("/confirmproductdeletion_" + productId, "✅ Подтвердить",
                                                    "/cancelproductdeletion_" + productId, "❌ Отменить"),
                                                    2, InlineKeyboardMarkup.class);
    
            userStates.put(chatId, BotState.CONFIRMING_PRODUCT_DELETION);
            sender.editMessage(chatId, messageId, message, confirmationKeyboard, false);
        }
    }

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
    }

    private boolean isThisProductAssignedToUser(Long productId, Long userId) {
        Products product = connection.getProductById(productId);
        if (product != null) {
            Categories category = connection.getCategoryById(product.assignedCategoryId());
            if (category != null) {
                ShoppingCarts cart = connection.getCartById(category.assignedCartId());
                if (cart != null) {
                    Set<Users> users = connection.getUsersAssignedToCart(cart.cartId());
                    if (!users.isEmpty()) {
                        for (Users user : users) {
                            if (user.userId().equals(userId)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}

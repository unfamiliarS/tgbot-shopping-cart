package com.shavarushka.commands.keyboard;

import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Users;

public class ReplyKeyboardHandler implements CartSelectionListener {
    protected final MessageSender sender;
    private final SQLiteConnection connection;

    public ReplyKeyboardHandler(MessageSender sender, SQLiteConnection connection, SelectedCartNotifier notifier) {
        this.sender = sender;
        this.connection = connection;
        notifier.addCartSelectionListener(this);
    }

    @Override
    public void onCartSelected(Long userId, Long cartId) {
        try {
            Long chatId = getChatIdByUserId(userId);
            
            if (chatId != null) {
                updateKeyboard(cartId, chatId, (cartId != null && cartId != 0));
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Long getChatIdByUserId(Long userId) {
        Users user = connection.getUserById(userId);
        return user != null ? user.chatId() : null;
    }

    private void updateKeyboard(Long cartId, Long chatId, boolean hasCart) throws TelegramApiException {
        ReplyKeyboard keyboard;
        String message;

        if (hasCart) {
            String cartName = connection.getCartById(cartId).cartName();
            keyboard = KeyboardsFabrics.createKeyboard(
                getCategoriesWithCreateNew(cartId),1, ReplyKeyboardMarkup.class
            );
            message = "Корзина: *" + MessageSender.escapeMarkdownV2(cartName) + "*";
        } else {
            keyboard = new ReplyKeyboardRemove(true);
            message = "Ты не состоишь не в одной корзине";
        }
        sender.sendMessage(chatId, message, keyboard, true);
    }

    private Map<String, String> getCategories(Long cartId) {
        Map<String, String> result = new HashMap<>();
        int cntr = 0; 
        for (Categories category : connection.getCategoriesByCartId(cartId)) {
            result.put("category" + ++cntr, category.categoryName());
        }
        return result;
    }

    private Map<String, String> getCategoriesWithCreateNew(Long cartId) {
        Map<String, String> categories = getCategories(cartId);
        categories.put("create_new_category", "Создать категорию");
        return categories;
    }

    private boolean isDefaultCategoryEmpty(Long cartId) {
        Categories defaultCategory = connection.getCategoryByAssignedCartIdAndName(cartId, "Прочее");
        if (connection.getProductsByCategoryId(defaultCategory.categoryId()).isEmpty()) {
            return true;
        }
        return false;
    }
}
package com.shavarushka.commands.callbackhandlers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.AbstractCallbackCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class ChangeCategoryCallback extends AbstractCallbackCommand {

    public ChangeCategoryCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }
    
    @Override
    public String getCommand() {
        return "/changecategoryfor_"; // + productId
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String message;
        
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;
        
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Users user = connection.getUserById(userId);
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Products product;
        
        if (!checkForProductExisting(chatId, messageId, productId) || !checkIfProductInCurrentUserCart(chatId, messageId, userId, productId))
            return;

        product = connection.getProductById(productId);
        message = product.fullURL() + "\n";

        message += "Выбери новую категорию:";
        var keyboard = getKeyboardWithCategories(
            connection.getCategoriesByCartId(user.selectedCartId()),
            productId,
            product.assignedCategoryId()
        );
        sender.editMessage(chatId, messageId, message, keyboard, false);
    }

    private InlineKeyboardMarkup getKeyboardWithCategories(Set<Categories> categories, Long productId, Long assignedCategoryId) {
        Map<String, String> buttons = new LinkedHashMap<>();
        categories.stream()
            .sorted(Comparator.comparing(Categories::creationTime))
            .forEach(category -> {
                buttons.put("/setcat_" + category.categoryId() + "_" + productId, category.categoryName());
                if (category.categoryId().equals(assignedCategoryId)) {
                    buttons.put("/setcat_" + category.categoryId() + "_" + productId, "✅ " + category.categoryName());
                }
            });
        return KeyboardsFabrics.createKeyboard(buttons,2, InlineKeyboardMarkup.class);
    }
}

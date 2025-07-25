package com.shavarushka.commands.commandhandlers;

import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class ListProductsOfCategoryCommand extends AbstractTextCommand {
    public ListProductsOfCategoryCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }
    
    @Override
    public String getCommand() {
        return "ListProductsOfCategory";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return false;

        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message = update.getMessage().getText();
        return !userStates.containsKey(chatId) &&
                isCommandContainsCategoryName(userId, message); 
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        
        if (!checkForUserExisting(chatId, userId) || !checkForCartExisting(chatId, userId))
            return;

        String categoryName = update.getMessage().getText();
        Users user = connection.getUserById(userId);

        Long categoryId = connection.getCategoryByAssignedCartIdAndName(user.selectedCartId(), categoryName).categoryId();
        Set<Products> products = connection.getProductsByCategoryId(categoryId);
        if (!products.isEmpty()) {
            for (Products product : products) {
                var keyboard = getProductKeyboard(product);
                sender.sendMessage(chatId, product.fullURL(), keyboard, false);
            }
        } else {
            sender.sendMessage(chatId, "Категория пуста", true);
        }
    }

    private boolean isCommandContainsCategoryName(Long userId, String categoryName) {
        Users user = connection.getUserById(userId);
        if (user != null && user.selectedCartId() != null) {
            Categories category = connection.getCategoryByAssignedCartIdAndName(user.selectedCartId(), categoryName);
            if (category != null) {
                return category.categoryName() != null;
            }
        }
        return false;
    }
}

package com.shavarushka.commands.commandhandlers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Settings;
import com.shavarushka.database.entities.Users;

public class ListProductsOfCategoryCommand extends AbstractTextCommand {

    private final int MESSAGES_PER_SECOND = 4;
    private final long DELAY_BETWEEN_MESSAGES_MS = 1000 / MESSAGES_PER_SECOND;
    
    public ListProductsOfCategoryCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }
    
    @Override
    public String getCommand() {
        return "ListProductsOfCategory";
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

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String categoryName = update.getMessage().getText();
        Users user = connection.getUserById(userId);
        Settings setting = connection.getSettingsById(userId);
        Long categoryId = connection.getCategoryByAssignedCartIdAndName(user.selectedCartId(), categoryName).categoryId();
        Set<Products> products = connection.getProductsByCategoryId(categoryId);
        int totalCountOfTheProducts = getNumberOfProductsToDisplay(products, setting.listAlreadyPurchased());
        
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        if (!products.isEmpty() && totalCountOfTheProducts != 0) {
            DisplayProducts(chatId, products, setting.listAlreadyPurchased());
            String message = "Всего товаров: " + totalCountOfTheProducts;
            sender.sendMessage(chatId, message, false);
        } else {
            sender.sendMessage(chatId, "Категория пуста", true);
        }
    }

    private int getNumberOfProductsToDisplay(Set<Products> allProductsInTheCategory, boolean listAlreadyPurchased) {
        if (listAlreadyPurchased) {
            return allProductsInTheCategory.size();
        }
        return (int) allProductsInTheCategory.stream()
                .filter(product -> !product.productPurchaseStatus())
                .count();
    }

    private void DisplayProducts(Long chatId, Set<Products> products, boolean listAlreadyPurchased) throws TelegramApiException {
        List<Products> sortedProducts = getSortProducts(products, listAlreadyPurchased);
        sendProductsWithRateLimit(chatId, sortedProducts);
    }

    private List<Products> getSortProducts(Set<Products> productsToSort, boolean listAlreadyPurchased) {
        if (listAlreadyPurchased) {
            return productsToSort.stream()
                .sorted(Comparator.comparing(Products::addingTime))
                .collect(Collectors.toList());        
        } else {
            return productsToSort.stream()
                .filter(product -> !product.productPurchaseStatus())
                .sorted(Comparator.comparing(Products::addingTime))
                .collect(Collectors.toList());             
        }
    }

    private void sendProductsWithRateLimit(Long chatId, List<Products> products) throws TelegramApiException {
        for (int i = 0; i < products.size(); i++) {
            var product = products.get(i);
            var keyboard = getProductKeyboard(product);
            if (i > 0) {
                try {
                    Thread.sleep(DELAY_BETWEEN_MESSAGES_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new TelegramApiException("Отправка прервана", e);
                }
            }
            
            sender.sendMessage(chatId, product.fullURL(), keyboard, false);
        }
    }
}

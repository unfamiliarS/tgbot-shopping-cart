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
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
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
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        Users user = connection.getUserById(userId);
        Long productId = extractIdFromMessage(update.getCallbackQuery().getData());
        Products product;
        String message;
        
        if (user.selectedCartId() == null) {
            message = "У тебя нет ни одной корзины😔 \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return;
        }
        if (!isProductExist(productId)) {
            sender.deleteMessage(chatId, messageId);
            return;
        }

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

    private boolean isProductExist(Long productId) {
        return connection.getProductById(productId) != null;
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

    public class ConfirmCategoryChangingCallback extends AbstractCallbackCommand {
        public ConfirmCategoryChangingCallback(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
        }

        @Override
        public String getCommand() {
            return "/setcat_"; // + categoryId _ productId
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long[] categoryId_productId = extractTwoIdFromMessage(update.getCallbackQuery().getData());
            Long categoryId = categoryId_productId[0];
            Long productId = categoryId_productId[1];
            Products product = connection.getProductById(productId);
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (product != null && connection.getCategoryById(categoryId) != null) {
                if (categoryId.equals(product.assignedCategoryId())) {
                    String message = product.fullURL();
                    var keyboard = KeyboardsFabrics.createKeyboard(
                        Map.of(
                            "/purchasestatus_" + productId, product.productPurchaseStatusAsString(),
                            "/changecategoryfor_" + productId, "Сменить категорию",
                            "/deleteproduct_" + productId, "🗑"
                        ),
                        2,
                        InlineKeyboardMarkup.class
                    );
                    sender.editMessage(chatId, messageId, message, keyboard, false);
                } else if (connection.updateCategoryForProduct(productId, categoryId)) {
                    System.out.println("Category was changed");
                    sender.deleteMessage(chatId, messageId);
                }                
            } else {
                sender.deleteMessage(chatId, messageId);
            }
        }
    }
}

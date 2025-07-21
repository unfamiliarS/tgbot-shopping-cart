package com.shavarushka.commands.commandhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.SelectedCartNotifierCommand;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.commands.keyboard.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class AddProductCommand extends SelectedCartNotifierCommand {
    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates,
                            List<CartSelectionListener> listeners, SQLiteConnection connection) {
        super(sender, userStates, connection, listeners);
    }

    @Override
    public String getCommand() {
        return "Добавить товар";
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
        String message = update.getMessage().getText();
        String regexURL = "^https://.*$";
        return !userStates.containsKey(chatId) &&
                message.matches(regexURL);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String productURL = update.getMessage().getText().strip();
        Users user = connection.getUserById(userId);
        boolean isNeedToNotify = false;
        String message;
        
        Long cartId = user.selectedCartId();
        if (cartId == null) {
            message = "У тебя нет ни одной корзины😔 \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return;
        }
        
        Products product = connection.getProductByUrlAndCart(productURL, cartId);
        
        // if product already exist
        if (product != null) {
            connection.getCategoryById(product.assignedCategoryId());
            message = "Этот товар уже есть в твоей корзине в категории *"
                + connection.getCategoryById(product.assignedCategoryId()).categoryName()
                + "*";
            sender.sendMessage(chatId, message, true);
        // if product is new
        } else {
            Categories defaultCategory = connection.getCategoryByAssignedCartIdAndName(cartId, "Прочее");
            Long defaultCategoryId;
            // if defaultCategory isn't create yet
            if (defaultCategory == null) {
                defaultCategoryId = connection.addCategory(new Categories(null, 
                                                                                cartId,
                                                                                "Прочее",
                                                                                null)
                );
                isNeedToNotify = true;
            } else {
                defaultCategoryId = defaultCategory.categoryId();
            }
            
            Long productId = connection.addProduct(new Products(
                                                null,
                                                productURL,
                                                defaultCategoryId,
                                                null,
                                                null,
                                                null)
            );

            if (isNeedToNotify)
                notifyCartSelectionListeners(userId, cartId);

            if (productId != null) {
                message = "Товар успешно добавлен в категорию *Прочее* 😎\n" + MessageSender.escapeMarkdownV2(productURL);
                InlineKeyboardMarkup keyboard = KeyboardsFabrics.createKeyboard(
                    Map.of(
                        "/changecategory", "Сменить категорию",
                        "/deleteproduct_" + productId, "🗑"
                    ),
                    2,
                    InlineKeyboardMarkup.class
                );
                sender.sendMessage(chatId, message, keyboard, true);
            } else {
                sender.sendMessage(chatId, "Ошибка при добавлении товара", false);
            }
        }
    }
}

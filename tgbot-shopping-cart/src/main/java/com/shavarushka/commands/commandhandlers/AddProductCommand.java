package com.shavarushka.commands.commandhandlers;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.commandhandlers.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class AddProductCommand extends SelectedCartNotifier {
    private final SQLiteConnection connection;

    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates,
                            List<CartSelectionListener> listeners, SQLiteConnection connection) {
        super(sender, userStates, listeners);
        this.connection = connection;
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
            notifyCartSelectionListeners(userId, cartId);

            if (productId != null) {
                sender.sendMessage(chatId, "Товар успешно добавлен в категорию *Прочее* 😎", true);
            } else {
                sender.sendMessage(chatId, "Ошибка при добавлении товара", false);
            }
        }
    }
}

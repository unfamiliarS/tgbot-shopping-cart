package com.shavarushka.commands.commandhandlers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.SettingsDependantNotifier;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class AddProductCommand extends AbstractTextCommand {

    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
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
        String regexURL = "(?s).*https?://.*";
        return !userStates.containsKey(chatId) &&
                message.matches(regexURL);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
    
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        String productURL = extractUrlFromMessage(update.getMessage().getText().strip());
        Users user = connection.getUserById(userId);
        boolean isNeedToNotifyKeyboardUpdate = false;
        
        Long cartId = user.selectedCartId();
        if (productURL.isEmpty()) {
            System.out.println("don't find url from message");
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
                defaultCategoryId = connection.addCategory(new Categories(cartId, "Прочее"));
                isNeedToNotifyKeyboardUpdate = true;
            } else {
                defaultCategoryId = defaultCategory.categoryId();
            }
            
            product = new Products(productURL, defaultCategoryId);
            Long productId = connection.addProduct(product);

            if (isNeedToNotifyKeyboardUpdate)
                updateReplyKeyboard(userId, cartId);

            if (productId != null) {
                product = connection.getProductById(productId);
                message = "Товар успешно добавлен в категорию *Прочее* 😎\n" + MessageSender.escapeMarkdownV2(productURL);
                var keyboard = getProductKeyboard(product);
                sender.sendMessage(chatId, message, keyboard, true);

                message = "добавил(а) новый товар\n" + productURL;
                notifyAllIfEnabled(user.userId(), cartId, SettingsDependantNotifier.NotificationType.PRODUCT_ADDED, message);
            } else {
                sender.sendMessage(chatId, "Ошибка при добавлении товара", false);
            }
        }
    }

    private String extractUrlFromMessage(String message) {
        String urlRegex = "https?://\\S+";
        
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return "";
    }
}

package com.shavarushka.commands.commandhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private MessageParser parser;

    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
        parser = new MessageParser();
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
        if (isMessage(update)) {
            Long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            return !isUserHaveAnyState(chatId) && parser.isMessageContainsAnyURLs(message);
        } else {
            return false;
        }
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String productURL = parser.extractUrlFromMessage(update.getMessage().getText().strip());
        Users user = connection.getUserById(userId);

        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;
        
        processAddingProduct(user, productURL);
    }

    private void processAddingProduct(Users user, String productURL) throws TelegramApiException {
        if (isProductExists(productURL, user.selectedCartId())) {
            Products product = connection.getProductByUrlAndCart(productURL, user.selectedCartId());
            sendMessageThatProductAlreadyExists(user.chatId(), product.assignedCategoryId());
        } else if (addProductInTheDefaultCategory(productURL, user.selectedCartId(), user.userId())) {
            sendMessageThatProductWasAdded(user.chatId(), productURL, user.selectedCartId());
                
            String message = "добавил(а) новый товар\n" + productURL;
            notifyAllIfEnabled(user.userId(), user.selectedCartId(), SettingsDependantNotifier.NotificationType.PRODUCT_ADDED, message);
        } else {
            sendErrorMessage(user.chatId());
        }
    }

    private boolean addProductInTheDefaultCategory(String productURL, Long cartId, Long userId) throws TelegramApiException {
        Categories defaultCategory = getDefaultCategory(userId, cartId);
        Long newProductId = addProductIntoDataBase(productURL, defaultCategory.categoryId());
        return newProductId != null;
    }

    private Long addProductIntoDataBase(String productURL, Long categoryId) {
        return connection.addProduct(new Products(productURL, categoryId));
    }

    private void sendMessageThatProductAlreadyExists(Long chatId, Long inThatCategoryProductContains) throws TelegramApiException {
        Categories category = connection.getCategoryById(inThatCategoryProductContains);
        String message = "Этот товар уже есть в твоей корзине в категории *"
                + category.categoryName()
                + "*";
        sender.sendMessage(chatId, message, true);
    }

    private void sendMessageThatProductWasAdded(Long chatId, String productURL, Long assignedCartId) throws TelegramApiException {
        Products product = connection.getProductByUrlAndCart(productURL, assignedCartId);
        String message = "Товар успешно добавлен в категорию *" + Categories.DEFAULT_CATEGORY_NAME + "* 😎\n"
                       + MessageSender.escapeMarkdownV2(product.fullURL());
        var keyboard = getProductKeyboard(product);
        sender.sendMessage(chatId, message, keyboard, true);
    }

    private void sendErrorMessage(Long chatId) throws TelegramApiException {
        sender.sendMessage(chatId, "❌ Ошибка при добавлении товара", false);
    }

    private static class MessageParser {
        
        private final List<Pattern> possibleURLs;

        MessageParser() {
            possibleURLs = getUrlRegexes("https?://\\S+");
        }

        private List<Pattern> getUrlRegexes(String ... possibleURLs) {
            List<Pattern> urlRegexes = new ArrayList<>(possibleURLs.length);
            for (int i = 0; i < possibleURLs.length; i++) {
                urlRegexes.add(Pattern.compile(possibleURLs[i]));
            }
            return urlRegexes;
        }
        
        private boolean isMessageContainsAnyURLs(String message) {
            return !extractUrlFromMessage(message).isEmpty();
        }

        private String extractUrlFromMessage(String message) {
            for (Pattern url : possibleURLs) {
                var matcher = url.matcher(message);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
            return "";
        }
    }
}

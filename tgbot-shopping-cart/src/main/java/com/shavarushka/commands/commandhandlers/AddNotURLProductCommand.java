package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.SettingsDependantNotifier;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class AddNotURLProductCommand extends AbstractTextCommand {

    public AddNotURLProductCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "Добавить товар (не URL)";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (isThisMessage(update)) {
            Long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            return !isUserHaveAnyState(chatId) && message.equals("+");
        } else {
            return false;
        }
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        sendInitialMessage(chatId);
        userStates.put(chatId, BotState.WAITING_FOR_PRODUCT_NAME);
    }

    private void sendInitialMessage(Long chatId) throws TelegramApiException {
        String message = "Введи название и описание нового товара:";
        var keyboard = KeyboardsFabrics.createKeyboard(
            Map.of("/canceladdingproduct", "Отменить добавление"), 1, InlineKeyboardMarkup.class
        );
        sender.sendMessage(chatId, message, keyboard, false);
    }

    public class ProductNameInputHandler extends AbstractTextCommand {

        public ProductNameInputHandler(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
            super(sender, userStates, connection);
        }

        @Override
        public String getCommand() {
            return "ProductNameInputHandler";
        }

        @Override
        public boolean shouldProcess(Update update) {
            if (isThisMessage(update)) {
                Long chatId = update.getMessage().getChatId();
                return isUserHaveState(chatId, BotState.WAITING_FOR_PRODUCT_NAME);
            }
            return false;
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();
            Users user = connection.getUserById(userId);
            String productName = update.getMessage().getText();

            if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId)) {
                userStates.remove(chatId);
                return;
            }

            processAddingProduct(user, productName);

            userStates.remove(chatId);
        }

        private void processAddingProduct(Users user, String productName) throws TelegramApiException {
            if (isProductExists(productName, user.selectedCartId())) {
                sendMessageForAlreadyExistingProduct(user, productName);
            }
            if (addProductInTheDefaultCategory(productName, user.selectedCartId(), user.userId())) {
                sendMessageThatProductWasAdded(user.chatId(), productName, user.selectedCartId());
                processUserNotification(user.userId(), user.selectedCartId(), productName);    
            } else {
                sendErrorMessage(user.chatId());
            }
        }

        private boolean addProductInTheDefaultCategory(String productName, Long cartId, Long userId) throws TelegramApiException {
            Categories defaultCategory = getDefaultCategory(userId, cartId);
            return connection.addProduct(new Products(productName, defaultCategory.categoryId())) != null;
        }

        private void sendMessageForAlreadyExistingProduct(Users user, String existingProductURL) throws TelegramApiException {
            Products product = connection.getProductByUrlAndCart(existingProductURL, user.selectedCartId());
            Categories categoryInThatProductContains = connection.getCategoryById(product.assignedCategoryId());
            String message = "Этот товар уже есть в твоей корзине в категории *"
                    + categoryInThatProductContains.categoryName()
                    + "*";
            sender.sendMessage(user.chatId(), message, true);
        }

        private void sendMessageThatProductWasAdded(Long chatId, String productName, Long assignedCartId) throws TelegramApiException {
            Products product = connection.getProductByUrlAndCart(productName, assignedCartId);
            String message = "Товар успешно добавлен в категорию *" + Categories.DEFAULT_CATEGORY_NAME + "* 😎\n\n"
                        + MessageSender.escapeMarkdownV2(product.fullURL());
            var keyboard = getProductKeyboard(product);
            sender.sendMessage(chatId, message, keyboard, true);
        }

        private void processUserNotification(Long userNotifierId, Long cartId, String productName) throws TelegramApiException {
            String message = "добавил(а) новый товар\n\n" + productName;
            notifyAllIfEnabled(userNotifierId, cartId, SettingsDependantNotifier.NotificationType.PRODUCT_ADDED, message);
        }

        private void sendErrorMessage(Long chatId) throws TelegramApiException {
            sender.sendMessage(chatId, "❌ Ошибка при добавлении товара", false);
        }
    }
}

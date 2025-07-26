package com.shavarushka.commands.interfaces;

import java.util.LinkedHashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Settings;
import com.shavarushka.database.entities.Users;

public abstract class AbstractCommand implements BotCommand, SettingsDependantNotifier, ReplyKeyboardUpdater {
    protected final MessageSender sender;
    protected final Map<Long, BotState> userStates;
    protected final SQLiteConnection connection;

    public AbstractCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        this.sender = sender;
        this.userStates = userStates;
        this.connection = connection;
    }

    @Override
    public boolean shouldProcess(Update update) {
        Long chatId;
        String message;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            message = update.getMessage().getText();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            message = update.getCallbackQuery().getData();
        } else {
            return false;
        }

        return !userStates.containsKey(chatId) &&
                message.startsWith(getCommand().strip());
    }

    /* 
     * Inline keyboards
     */
    protected InlineKeyboardMarkup getProductKeyboard(Products product) {
        Map<String, String> buttons = new LinkedHashMap<>();
        buttons.put("/deleteproduct_" + product.productId(), "🗑 Удалить");
        buttons.put("/purchasestatus_" + product.productId(), product.productPurchaseStatusAsString());
        buttons.put("/changecategoryfor_" + product.productId(), "🔄 Сменить категорию");
        return KeyboardsFabrics.createKeyboard(buttons, 2, InlineKeyboardMarkup.class);
    }

    protected InlineKeyboardMarkup getSettingsKeyboard(Settings settings) {
        Map<String, String> buttons = new LinkedHashMap<>();
        String mark;
        mark = settings.listAlreadyPurchased().equals(true) ? "✅ " : "";
        buttons.put("/listalreadypurchased", mark + "Показывать купленные товары 💚 💛");
        mark = settings.notifyAboutProducts().equals(true) ? "✅ " : "";
        buttons.put("/notifyaboutproducts", mark + "Уведомлять о действиях с товарами 🔔");
        mark = settings.notifyAboutInviting().equals(true) ? "✅ " : "";
        buttons.put("/notifyaboutinviting", mark + "Уведомлять о cостоянии приглашения в корзину 🔔");
        buttons.put("/close", "✖ Закрыть");
        return KeyboardsFabrics.createKeyboard(buttons, 1, InlineKeyboardMarkup.class);
    }

    /* 
     * Base checkups for data existing
     */
    protected boolean checkForUserExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getUserById(userId) == null) {
            message = "Не могу найти тебя в своей базе😔 Вероятно мы ещё не знакомы.\n/start чтобы поздороваться";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForCartExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getUserById(userId).selectedCartId() == null) {
            message = "У тебя нет ни одной корзины😔 \n/createnewcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForProductExisting(Long chatId, Integer messageId, Long productId) throws TelegramApiException {
        if (connection.getProductById(productId) == null) {
            sender.deleteMessage(chatId, messageId);
            return false;
        }
        return true;
    }

    /* 
     * Settings notification
     */
    public void notifyAllIfEnabled(Long userNotifier, Long cartId, NotificationType type, String message) throws TelegramApiException {
        for (Users user : connection.getUsersAssignedToCart(cartId)) {
            notifyIfEnabled(userNotifier, user.userId(), message, type);
        }
    }   
    
    @Override
    public boolean shouldNotify(Long userId, NotificationType type) {
        var settings = connection.getSettingsById(userId);
        return switch (type) {
            case PRODUCT_ADDED, CATEGORY_ADDED,
                 PRODUCT_DELETED, CATEGORY_DELETED
                    -> settings.notifyAboutProducts();
            case CONFIRMATION_OF_JOINING_THE_CART,
                 REFUSING_OF_JOINING_THE_CART
                    -> settings.notifyAboutInviting();
        };
    }

    @Override
    public void sendNotification(Long userNotifierId, Long userId, String message) throws TelegramApiException {
        Users userNotifier = connection.getUserById(userNotifierId);
        Users user = connection.getUserById(userId);
        String userNotifierName = userNotifier.username() != null
            ? "@" + userNotifier.username()
            : userNotifier.firstname()
            + " ";
        if (user.chatId() != null) {
            sender.sendMessage(user.chatId(), "🔔 " + userNotifierName + " " + message, false);
        }
    }

    /* 
     * Reply Keyboard update methods
     */
    @Override
    public void updateReplyKeyboardOnDataChanges(Long userId, Long cartId) throws TelegramApiException {
        Users user = connection.getUserById(userId);
        Long chatId = user != null ? user.chatId() : null;
            
        if (chatId != null) {
            updateKeyboard(cartId, chatId, cartId != null);
        }
    }

    private void updateKeyboard(Long cartId, Long chatId, boolean hasCart) throws TelegramApiException {
        ReplyKeyboard keyboard;
        String message;

        if (hasCart) {
            String cartName = connection.getCartById(cartId).cartName();
            keyboard = KeyboardsFabrics.createKeyboard(
                getCategoriesWithCreateNewAndDelete(cartId),2, ReplyKeyboardMarkup.class
            );
            message = "Корзина: *" + MessageSender.escapeMarkdownV2(cartName) + "*";
        } else {
            keyboard = new ReplyKeyboardRemove(true);
            message = "Ты не состоишь не в одной корзине";
        }
        sender.sendMessage(chatId, message, keyboard, true);
    }

    private Map<String, String> getCategories(Long cartId) {
        Map<String, String> result = new LinkedHashMap<>();
        int cntr = 0;
        Categories defCategory = null;
        for (Categories category : connection.getCategoriesByCartId(cartId)) {
            if (category.categoryName().equals("Прочее")) {
                defCategory = category;
                continue;
            }
            result.put("category" + ++cntr, category.categoryName());
        }
        if (defCategory != null)
            result.put("category" + ++cntr, defCategory.categoryName());
        return result;
    }

    private Map<String, String> getCategoriesWithDelete(Long cartId) {
        Map<String, String> categories = getCategories(cartId);
        categories.put("delete_category", "Удалить категорию");
        return categories;
    }

    private Map<String, String> getCategoriesWithCreateNewAndDelete(Long cartId) {
        Map<String, String> categories = getCategoriesWithDelete(cartId);
        categories.put("create_new_category", "Создать категорию");
        return categories;
    }
}

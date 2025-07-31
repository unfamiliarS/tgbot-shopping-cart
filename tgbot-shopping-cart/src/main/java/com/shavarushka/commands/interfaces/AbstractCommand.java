package com.shavarushka.commands.interfaces;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
import com.shavarushka.database.entities.ShoppingCarts;
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
        if (isThisMessage(update)) {
            chatId = update.getMessage().getChatId();
            message = update.getMessage().getText();
        } else if (isThisCallback(update)) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            message = update.getCallbackQuery().getData();
        } else {
            return false;
        }
        return !isUserHaveAnyState(chatId) && message.startsWith(getCommand().strip());
    }

    protected Categories getDefaultCategory(Long userId, Long assignedCartId) throws TelegramApiException {
        if (!isDefaultCategoryExists(assignedCartId)) {
            connection.addCategory(new Categories(assignedCartId, Categories.DEFAULT_CATEGORY_NAME));
            updateReplyKeyboard(userId, assignedCartId);
        }
        return connection.getCategoryByAssignedCartIdAndName(assignedCartId, Categories.DEFAULT_CATEGORY_NAME);
    }

    /* 
     * Predicates
     */
    protected boolean isThisMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    protected boolean isThisCallback(Update update) {
        return update.hasCallbackQuery();
    }

    protected boolean isUserHaveState(Long chatId, BotState... states) {
        if (isUserHaveAnyState(chatId)) {
            for (BotState state : states) {
                if (userStates.get(chatId).equals(state)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isUserHaveAnyState(Long chatId) {
        return userStates.containsKey(chatId);
    }

    protected boolean isCategoryExists(String categoryName, Long assignedCartId) {
        return connection.getCategoryByAssignedCartIdAndName(assignedCartId, categoryName) != null;
    }

    protected boolean isDefaultCategoryExists(Long assignedCartId) {
        return connection.getCategoryByAssignedCartIdAndName(assignedCartId, Categories.DEFAULT_CATEGORY_NAME) != null;
    }
    
    protected boolean isProductExists(Long productId) {
        return connection.getProductById(productId) != null;
    }

    protected boolean isProductExists(String productURL, Long cartId) {
        return connection.getProductByUrlAndCart(productURL, cartId) != null;
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
        buttons.put("/listalreadypurchased", mark + "Показывать купленные товары 💚 🤍");
        mark = settings.notifyAboutProducts().equals(true) ? "✅ " : "";
        buttons.put("/notifyaboutproducts", mark + "Уведомлять о действиях с товарами 🔔");
        mark = settings.notifyAboutInviting().equals(true) ? "✅ " : "";
        buttons.put("/notifyaboutinviting", mark + "Уведомлять о cостоянии приглашения в корзину 🔔");
        buttons.put("/close", "✖ Закрыть");
        return KeyboardsFabrics.createKeyboard(buttons, 1, InlineKeyboardMarkup.class);
    }

    protected InlineKeyboardMarkup getMyCartsKeyboard(Set<ShoppingCarts> carts, Long selectedCartId) {
        Map<String, String> buttons = new LinkedHashMap<>();
        carts.stream()
            .sorted(Comparator.comparing(ShoppingCarts::creationTime))
            .forEach(cart -> {
                String cartName = cart.cartId().equals(selectedCartId) 
                    ? "✅ " + cart.cartName() 
                    : cart.cartName();
                buttons.put("/setcart_" + cart.cartId(), cartName);
                buttons.put("/deletecart_" + cart.cartId(), "🗑");
            });
            buttons.put("/close", "✖ Закрыть");
        return KeyboardsFabrics.createKeyboard(buttons,2, InlineKeyboardMarkup.class);
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

    protected boolean checkForAssignedCartExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getUserById(userId).selectedCartId() == null) {
            message = "У тебя не выбрана ни одна корзина😔 \n/createcart чтобы создать новую или /mycarts для выбора существующих";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForAnyAssignedCartsExisting(Long chatId, Long userId) throws TelegramApiException {
        String message;
        if (connection.getCartsAssignedToUser(userId).isEmpty()) {
            message = "У тебя нет ни одной корзины😔 \n/createcart чтобы создать";
            sender.sendMessage(chatId, message, false);
            return false;
        }
        return true;
    }

    protected boolean checkForCartExisting(Long cartId) throws TelegramApiException {
        return connection.getCartById(cartId) != null;
    }

    protected boolean checkForCategoryExisting(Long chatId, Integer messageId, Long categoryId) throws TelegramApiException {
        if (connection.getCategoryById(categoryId) == null) {
            sender.deleteMessage(chatId, messageId);
            return false;
        }
        return true;
    }

    protected boolean checkForProductExisting(Long chatId, Integer messageId, Long productId) throws TelegramApiException {
        if (!isProductExists(productId)) {
            sender.deleteMessage(chatId, messageId);
            return false;
        }
        return true;
    }

    protected boolean checkIfProductInCurrentUserCart(Long chatId, Integer messageId, Long userId, Long productId) throws TelegramApiException {
        Long currentlySelectedCart = connection.getUserById(userId).selectedCartId();
        Long productCart;

        Products product = connection.getProductById(productId);
        if (product != null) {
            Categories category = connection.getCategoryById(product.assignedCategoryId());
            if (category != null) {
                ShoppingCarts cart = connection.getCartById(category.assignedCartId());
                if (cart != null) {
                    productCart = cart.cartId();
                    if (currentlySelectedCart.equals(productCart))
                        return true;
                }
            }
        }
        sender.deleteMessage(chatId, messageId);
        return false;
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
    public void updateReplyKeyboard(Long userId, Long cartId) throws TelegramApiException {
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
            if (category.categoryName().equals(Categories.DEFAULT_CATEGORY_NAME)) {
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

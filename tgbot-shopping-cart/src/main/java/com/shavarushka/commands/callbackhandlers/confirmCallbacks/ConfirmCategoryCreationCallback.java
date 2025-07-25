package com.shavarushka.commands.callbackhandlers.confirmCallbacks;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.callbackhandlers.interfaces.SelectedCartNotifierCallback;
import com.shavarushka.commands.interfaces.SettingNotifyHandler;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Users;

public class ConfirmCategoryCreationCallback extends SelectedCartNotifierCallback {
    private final Map<Long, String> categoryNames;

    public ConfirmCategoryCreationCallback(MessageSender sender, Map<Long, BotState> userStates,
                                    SQLiteConnection connection, Map<Long, String> categoryNames,
                                    List<CartSelectionListener> listeners) {
        super(sender, userStates, connection, listeners);
        this.categoryNames = categoryNames;
    }

    @Override
    public String getCommand() {
        return "/confirmcategorycreation";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasCallbackQuery())
            return false;

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        return update.getCallbackQuery().getData().startsWith(getCommand().strip()) &&
               userStates.containsKey(chatId) &&
               userStates.get(chatId).equals(BotState.CONFIRMING_CATEGORY_CREATION);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String message;

        Users user = connection.getUserById(chatId);
        String categoryName = categoryNames.remove(userId);
        
        if (categoryName != null) {
            // create category
            Categories category = new Categories(null, user.selectedCartId(), categoryName, null);
            connection.addCategory(category);
    
            // notify to update keyboard on added category
            user = connection.getUserById(user.userId());            
            notifyCartSelectionListeners(user.userId(), user.selectedCartId());
            
            userStates.remove(chatId);
            message = "✅ Категория *" + MessageSender.escapeMarkdownV2(categoryName) + "* создана😎";
            sender.editMessage(chatId, messageId, message, true);

            message = "создал(а) новую категорию '" + categoryName + "'";
            notifyAllIfEnabled(user.userId(), user.selectedCartId(), SettingNotifyHandler.NotificationType.CATEGORY_ADDED, message);
        }
    }
}

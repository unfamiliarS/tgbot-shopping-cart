package com.shavarushka.commands.commandhandlers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.keyboards.KeyboardsFabrics;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Categories;

public class DeleteCategoryCommand extends AbstractTextCommand {
    public DeleteCategoryCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates, connection);
    }

    @Override
    public String getCommand() {
        return "Удалить категорию";
    }

    @Override
    public String getDescription() {
        return getCommand();
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message;
        
        if (!checkForUserExisting(chatId, userId) || !checkForAssignedCartExisting(chatId, userId))
            return;

        Long selectedCartId = connection.getUserById(userId).selectedCartId();
        Set<Categories> categories = connection.getCategoriesByCartId(selectedCartId);
        if (categories.size() == 0) {
            message = "У тебя нет ни одной категории, которую можно удалить";
            sender.sendMessage(chatId, message, false);
            return;
        }
        message = "Выбери какую категорию хочешь удалить";
        var keyboard = getKeyboardWithCategories(categories);
        sender.sendMessage(chatId, message, keyboard, false);
    }

    private InlineKeyboardMarkup getKeyboardWithCategories(Set<Categories> categories) {
        Map<String, String> buttons = new LinkedHashMap<>();
        categories.stream()
            .sorted(Comparator.comparing(Categories::creationTime))
            .forEach(category -> {
                buttons.put("/deletecategory_" + category.categoryId(), "🗑 " + category.categoryName());
            });
            buttons.put("/close", "❌ Отменить");
        return KeyboardsFabrics.createKeyboard(buttons,1, InlineKeyboardMarkup.class);
    }
}

package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.database.SQLiteConnection;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Users;

public class AddProductCommand extends AbstractTextCommand {
    private final SQLiteConnection connection;

    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates, SQLiteConnection connection) {
        super(sender, userStates);
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
        String productURL = update.getMessage().getText();
        Users user = connection.getUserById(userId);
        Products product;
        String message;

        if ((product = connection.getProductByUrl(productURL)) != null) {
            connection.addProductToCartIntermediate(product.productId(), user.selectedCartId());
        } else {
            product = new Products(null,
                                   productURL,
                                   user.selectedCartId(),
                                   null,
                                   null,
                                   null
            );
            connection.addProduct(product, user.selectedCartId());
        }
        
        message = update.getMessage().getText() + "\nуспешно добавлен в корзину😎";
        sender.sendMessage(chatId, message, false);
    }
}

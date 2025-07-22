package com.shavarushka.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.callbackhandlers.*;
import com.shavarushka.commands.commandhandlers.*;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.commands.keyboard.ReplyKeyboardHandler;
import com.shavarushka.database.SQLiteConnection;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, String> tempNewNames = new HashMap<>();
    private final List<CartSelectionListener> selectedCartsListeners = new ArrayList<>();
    private final MessageSender sender;
    private final SQLiteConnection connection;

    public CommandManager(TelegramClient telegramClient) {
        sender = new MessageSender(telegramClient);
        connection = new SQLiteConnection(System.getenv("DB_URL"));

        // register commands
        registerCommand(new StartCommand(sender, userStates, connection, selectedCartsListeners));
        var createCartCommand = new CreateCartCommand(sender, userStates, connection, tempNewNames);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(sender, userStates, connection));
        var mycartCommand = new MyCartCommand(sender, userStates, connection);
        registerCommand(mycartCommand);
        var inviteUserCommand = new InviteUserCommand(sender, userStates, connection);
        registerCommand(inviteUserCommand);
        registerCommand(inviteUserCommand.new UsernameInputHandler(sender, userStates, connection));
        registerCommand(new AddProductCommand(sender, userStates, selectedCartsListeners, connection));
        registerCommand(new ListProductsOfCategoryCommand(sender, userStates, connection));
        var addCategoryCommand = new AddCategoryCommand(sender, userStates, connection, tempNewNames, selectedCartsListeners);
        registerCommand(addCategoryCommand);
        registerCommand(addCategoryCommand.new CategoryNameInputHandler(sender, userStates, connection));
        registerCommand(new ConfirmCategoryCreationCallback(sender, userStates, connection, tempNewNames, selectedCartsListeners));
        registerCommand(new CancelCreatingCategoryCallback(sender, userStates, connection));
        registerCommand(new DeleteCategoryCommand(sender, userStates, connection));

        // register callbacks
        registerCommand(new CancelCreatingCartCallback(sender, userStates, connection));
        registerCommand(new ConfirmCartCreationCallback(sender, userStates, connection, tempNewNames, selectedCartsListeners));
        registerCommand(mycartCommand.new SetCartCallback(sender, userStates, connection, selectedCartsListeners));
        registerCommand(new CancelInvitingUserCallback(sender, userStates, connection));
        var confirmInvitingCallback = new ConfirmInvitingCallback(sender, userStates, connection, selectedCartsListeners);
        registerCommand(confirmInvitingCallback);
        registerCommand(new DeleteCartCallback(sender, userStates, connection));
        registerCommand(new CancelCartDeletionCallback(sender, userStates, connection));
        registerCommand(new ConfirmCartDeletionCallback(sender, userStates, connection));
        registerCommand(new DeleteProductCallback(sender, userStates, connection));
        registerCommand(new ConfirmProductDeletionCallback(sender, userStates, connection));
        registerCommand(new CancelProductDeletionCallback(sender, userStates, connection));
        registerCommand(new DeleteCategoryCallback(sender, userStates, connection));
        registerCommand(new ConfirmCategoryDeletionCallback(sender, userStates, connection, selectedCartsListeners));
        registerCommand(new CancelCategoryDeletionCallback(sender, userStates, connection));
        var changecategory = new ChangeCategoryCallback(sender, userStates, connection);
        registerCommand(changecategory);
        registerCommand(changecategory.new ConfirmCategoryChangingCallback(sender, userStates, connection));
        registerCommand(new ChangePurchaseStatusCallback(sender, userStates, connection));

        // create a ReplyKeyboardHandler for correct updating keyboard on cart changes
        new ReplyKeyboardHandler(sender, connection, confirmInvitingCallback);
    }

    public void registerCommand(BotCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void processUpdate(Update update) throws TelegramApiException {
        boolean isUserRegister = isUserRegister(update);

        for (BotCommand command : commands.values()) {
            if (command.shouldProcess(update)) {
                if (!isUserRegister) {
                    command = commands.get("/start");
                }
                command.execute(update);
                return;
            }
        }
    }

    private boolean isUserRegister(Update update) throws TelegramApiException {
        Long userId;
        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
        } else {
            return false;
        }

        return connection.getUserById(userId) != null;
    }
}
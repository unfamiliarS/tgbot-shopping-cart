package com.shavarushka.commands;

import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.callbackhandlers.*;
import com.shavarushka.commands.callbackhandlers.cancelCallbacks.*;
import com.shavarushka.commands.callbackhandlers.confirmCallbacks.*;
import com.shavarushka.commands.callbackhandlers.deleteCallbacks.*;
import com.shavarushka.commands.callbackhandlers.settingCallbacks.*;
import com.shavarushka.commands.commandhandlers.*;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.database.SQLiteConnection;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, String> tempNewNames = new HashMap<>();
    private final MessageSender sender;
    private final SQLiteConnection connection;

    public CommandManager(TelegramClient telegramClient) {
        sender = new MessageSender(telegramClient);
        connection = new SQLiteConnection(System.getenv("DB_URL"));

        // Register commands
        registerCommand(new StartCommand(sender, userStates, connection));
        var createCartCommand = new CreateCartCommand(sender, userStates, connection, tempNewNames);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(sender, userStates, connection));
        registerCommand(new MyCartsCommand(sender, userStates, connection));
        var inviteUserCommand = new InviteUserCommand(sender, userStates, connection);
        registerCommand(inviteUserCommand);
        registerCommand(inviteUserCommand.new UsernameInputHandler(sender, userStates, connection));
        registerCommand(new AddProductCommand(sender, userStates, connection));
        registerCommand(new ListProductsOfCategoryCommand(sender, userStates, connection));
        var addCategoryCommand = new AddCategoryCommand(sender, userStates, connection, tempNewNames);
        registerCommand(addCategoryCommand);
        registerCommand(addCategoryCommand.new CategoryNameInputHandler(sender, userStates, connection));
        registerCommand(new ConfirmCategoryCreationCallback(sender, userStates, connection, tempNewNames));
        registerCommand(new CancelCreatingCategoryCallback(sender, userStates, connection));
        registerCommand(new DeleteCategoryCommand(sender, userStates, connection));
        registerCommand(new SettingsCommand(sender, userStates, connection));

        // Register callbacks
        registerCommand(new CancelCreatingCartCallback(sender, userStates, connection));
        registerCommand(new ConfirmCartCreationCallback(sender, userStates, connection, tempNewNames));
        registerCommand(new SetCartCallback(sender, userStates, connection));
        registerCommand(new CancelInvitingUserCallback(sender, userStates, connection));
        registerCommand(new ConfirmInvitingCallback(sender, userStates, connection));
        registerCommand(new DeleteCartCallback(sender, userStates, connection));
        registerCommand(new CancelCartDeletionCallback(sender, userStates, connection));
        registerCommand(new ConfirmCartDeletionCallback(sender, userStates, connection));
        registerCommand(new DeleteProductCallback(sender, userStates, connection));
        registerCommand(new ConfirmProductDeletionCallback(sender, userStates, connection));
        registerCommand(new CancelProductDeletionCallback(sender, userStates, connection));
        registerCommand(new DeleteCategoryCallback(sender, userStates, connection));
        registerCommand(new ConfirmCategoryDeletionCallback(sender, userStates, connection));
        registerCommand(new CancelCategoryDeletionCallback(sender, userStates, connection));
        var changecategory = new ChangeCategoryCallback(sender, userStates, connection);
        registerCommand(changecategory);
        registerCommand(changecategory.new ConfirmCategoryChangingCallback(sender, userStates, connection));
        registerCommand(new ChangePurchaseStatusCallback(sender, userStates, connection));
        registerCommand(new CloseCallback(sender, userStates, connection));
        // settings callbacks
        registerCommand(new SettingListAlreadyPurchasedCallback(sender, userStates, connection));
        registerCommand(new SettingNotifyAboutProductsCallback(sender, userStates, connection));
        registerCommand(new SettingNotifyAboutInvitingCallback(sender, userStates, connection));
        registerCommand(new RefuseInvitingCallback(sender, userStates, connection));
    }

    public void registerCommand(BotCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void processUpdate(Update update) throws TelegramApiException {
        for (BotCommand command : commands.values()) {
            if (command.shouldProcess(update)) {
                command.execute(update);
                return;
            }
        }
    }
}
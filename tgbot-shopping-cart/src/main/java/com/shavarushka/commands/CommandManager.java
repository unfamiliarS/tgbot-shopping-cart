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
import com.shavarushka.commands.generalcommandhandlers.CancelCartDeletion;
import com.shavarushka.commands.generalcommandhandlers.CancelCreatingCart;
import com.shavarushka.commands.generalcommandhandlers.CancelInvitingUser;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.commands.keyboard.ReplyKeyboardHandler;
import com.shavarushka.database.SQLiteConnection;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, String> tempNewCartNames = new HashMap<>();
    private final List<CartSelectionListener> selectedCartsListeners = new ArrayList<>();
    private final MessageSender sender;
    private SQLiteConnection connection;

    public CommandManager(TelegramClient telegramClient) {
        sender = new MessageSender(telegramClient);
        connection = new SQLiteConnection(System.getenv("DB_URL"));

        // register commands
        registerCommand(new StartCommand(sender, userStates, connection, selectedCartsListeners));
        // registerCommand(new HelpCommand(sender, userStates, commands));
        var createCartCommand = new CreateCartCommand(sender, userStates, tempNewCartNames);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(sender, userStates));
        var mycartCommand = new MyCartCommand(sender, userStates, connection);
        registerCommand(mycartCommand);
        var inviteUserCommand = new InviteUserCommand(sender, userStates, connection);
        registerCommand(inviteUserCommand);
        registerCommand(inviteUserCommand.new UsernameInputHandler(sender, userStates));
        registerCommand(new AddProductCommand(sender, userStates));

        // register callbacks
        registerCommand(new CancelCreatingCart(sender, userStates));
        registerCommand(new ConfirmCartCreationCallback(sender, userStates, connection, tempNewCartNames, selectedCartsListeners));
        registerCommand(mycartCommand.new SetCartCallback(sender, userStates, selectedCartsListeners));
        registerCommand(new CancelInvitingUser(sender, userStates));
        var confirmInvitingCallback = new ConfirmInvitingCallback(sender, userStates, connection, selectedCartsListeners);
        registerCommand(confirmInvitingCallback);
        registerCommand(new DeleteCartCallback(sender, userStates, connection));
        registerCommand(new CancelCartDeletion(sender, userStates));
        registerCommand(new ConfirmCartDeletion(sender, userStates, connection));

        // create a ReplyKeyboardHandler for correct updating keyboard on selected cart changes
        new ReplyKeyboardHandler(sender, connection, confirmInvitingCallback);
        
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
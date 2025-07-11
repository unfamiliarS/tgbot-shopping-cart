package com.shavarushka.commands;

import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.callbackhandlers.CancelCartDeletion;
import com.shavarushka.commands.callbackhandlers.CancelCreatingCartCallback;
import com.shavarushka.commands.callbackhandlers.CancelInvitingUserCallback;
import com.shavarushka.commands.callbackhandlers.ConfirmCartCreationCallback;
import com.shavarushka.commands.callbackhandlers.ConfirmCartDeletion;
import com.shavarushka.commands.callbackhandlers.ConfirmInvitingCallback;
import com.shavarushka.commands.callbackhandlers.DeleteCartCallback;
import com.shavarushka.commands.commandhandlers.CreateCartCommand;
import com.shavarushka.commands.commandhandlers.HelpCommand;
import com.shavarushka.commands.commandhandlers.InviteUserCommand;
import com.shavarushka.commands.commandhandlers.MyCartCommand;
import com.shavarushka.commands.commandhandlers.StartCommand;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.CallbackCommand;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.interfaces.TextCommand;
import com.shavarushka.database.SQLiteConnection;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    private final Map<Long, String> tempNewCartNames = new HashMap<>();
    private final MessageSender sender;
    private SQLiteConnection connection;

    public CommandManager(TelegramClient telegramClient) {
        sender = new MessageSender(telegramClient);
        connection = new SQLiteConnection(System.getenv("DB_URL"));

        // register commands
        registerCommand(new StartCommand(sender, userStates, connection));
        registerCommand(new HelpCommand(sender, userStates, commands));
        // registerCommand(new CancelCommand(sender, userStates));
        var createCartCommand = new CreateCartCommand(sender, userStates, tempNewCartNames);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(sender, userStates));
        var mycartCommand = new MyCartCommand(sender, userStates, connection);
        registerCommand(mycartCommand);
        var inviteUserCommand = new InviteUserCommand(sender, userStates, connection);
        registerCommand(inviteUserCommand);
        registerCommand(inviteUserCommand.new UsernameInputHandler(sender, userStates));

        // register callbacks
        registerCommand(new CancelCreatingCartCallback(sender, userStates));
        registerCommand(new ConfirmCartCreationCallback(sender, userStates, connection, tempNewCartNames));
        registerCommand(mycartCommand.new SetCartCallback(sender, userStates));
        registerCommand(new CancelInvitingUserCallback(sender, userStates));
        registerCommand(new ConfirmInvitingCallback(sender, userStates, connection));
        registerCommand(new DeleteCartCallback(sender, userStates, connection));
        registerCommand(new CancelCartDeletion(sender, userStates));
        registerCommand(new ConfirmCartDeletion(sender, userStates, connection));
    }

    public void registerCommand(TextCommand command) {
        commands.put(command.getCommand(), command);
    }

    public void registerCommand(CallbackCommand command) {
        commands.put(command.getCallbackPattern(), command);
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
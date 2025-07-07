package com.shavarushka.commands;

import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.callbackhandlers.CancelCreatingNewCartCallback;
import com.shavarushka.commands.callbackhandlers.ConfirmCartCreationCallback;
import com.shavarushka.commands.commandhandlers.CancelCommand;
import com.shavarushka.commands.commandhandlers.CreateCartCommand;
import com.shavarushka.commands.commandhandlers.HelpCommand;
import com.shavarushka.commands.commandhandlers.MyCartCommand;
import com.shavarushka.commands.commandhandlers.StartCommand;
import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.CallbackCommand;
import com.shavarushka.commands.interfaces.TextCommand;
import com.shavarushka.database.DatabaseOperations;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    private DatabaseOperations connection;

    public CommandManager(TelegramClient telegramClient) {
        connection = new DatabaseOperations();
        // register commands
        registerCommand(new StartCommand(telegramClient, userStates));
        registerCommand(new HelpCommand(telegramClient, userStates, commands));
        registerCommand(new CancelCommand(telegramClient, userStates));
        var createCartCommand = new CreateCartCommand(telegramClient, userStates);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(telegramClient, userStates));
        var mycartCommand = new MyCartCommand(telegramClient, userStates);
        registerCommand(mycartCommand);

        // register callbacks
        registerCommand(new CancelCreatingNewCartCallback(telegramClient, userStates));
        registerCommand(new ConfirmCartCreationCallback(telegramClient, userStates, connection));
        registerCommand(mycartCommand.new SetCartCallback(telegramClient, userStates));
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
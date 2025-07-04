package com.shavarushka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.callbackhandler.CancelCreatingNewCartCallback;
import com.shavarushka.commands.callbackhandler.ConfirmCartCreationCallback;
import com.shavarushka.commands.commandhandler.CancelCommand;
import com.shavarushka.commands.commandhandler.CreateCartCommand;
import com.shavarushka.commands.commandhandler.HelpCommand;
import com.shavarushka.commands.commandhandler.MyCartCommand;
import com.shavarushka.commands.commandhandler.StartCommand;
import com.shavarushka.commands.intr.BotCommand;
import com.shavarushka.commands.intr.BotState;
import com.shavarushka.commands.intr.CallbackCommand;
import com.shavarushka.commands.intr.TextCommand;

public class CommandManager {
    private final Map<String, BotCommand> commands = new HashMap<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    // private PostgreSQLConnection DBConnection;

    public CommandManager(TelegramClient telegramClient) {
        // DBConnection = new PostgreSQLConnection(System.getenv("DB_URL"),
        //                                         System.getenv("DB_USER"), 
        //                                         System.getenv("DB_PASSWORD"));
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
        registerCommand(new ConfirmCartCreationCallback(telegramClient, userStates));
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
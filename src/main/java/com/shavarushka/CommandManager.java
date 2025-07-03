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
import com.shavarushka.commands.commandhandler.StartCommand;
import com.shavarushka.commands.intr.BotState;
import com.shavarushka.commands.intr.CallbackCommand;
import com.shavarushka.commands.intr.TextCommand;

public class CommandManager {
    private final List<TextCommand> textCommands = new ArrayList<>();
    private final List<CallbackCommand> callbackCommands = new ArrayList<>();
    private final Map<Long, BotState> userStates = new HashMap<>();
    // private PostgreSQLConnection DBConnection;

    public CommandManager(TelegramClient telegramClient) {
        // DBConnection = new PostgreSQLConnection(System.getenv("DB_URL"),
        //                                         System.getenv("DB_USER"), 
        //                                         System.getenv("DB_PASSWORD"));
        // register commands
        registerCommand(new StartCommand(telegramClient, userStates));
        registerCommand(new HelpCommand(telegramClient, userStates, textCommands));
        registerCommand(new CancelCommand(telegramClient, userStates));
        var createCartCommand = new CreateCartCommand(telegramClient, userStates);
        registerCommand(createCartCommand);
        registerCommand(createCartCommand.new NameInputHandler(telegramClient, userStates));
        // register callbacks
        registerCommand(new CancelCreatingNewCartCallback(telegramClient, userStates));
        registerCommand(new ConfirmCartCreationCallback(telegramClient, userStates));
    }

    public void registerCommand(TextCommand command) {
        textCommands.add(command);
    }

    public void registerCommand(CallbackCommand command) {
        callbackCommands.add(command);
    }

    public void processUpdate(Update update) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            processTextCommand(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackCommand(update);
        } else {
            return;
        }
    }

    private void processTextCommand(Update update) throws TelegramApiException {
        for (TextCommand command : textCommands) {
            if (command.shouldProcess(update)) {
                command.execute(update);
                return;
            }
        }
    }
    
    private void processCallbackCommand(Update update) throws TelegramApiException {
        for (CallbackCommand command : callbackCommands) {
            if (command.shouldProcess(update)) {
                command.execute(update);
                return;
            }
        }
    }
}
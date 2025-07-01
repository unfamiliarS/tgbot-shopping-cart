package com.shavarushka;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.CreateCartCommand;
import com.shavarushka.commands.HelpCommand;
import com.shavarushka.commands.StartCommand;
import com.shavarushka.commands.intr.CallbackCommand;
import com.shavarushka.commands.intr.TextCommand;

public class CommandManager {
    private final List<TextCommand> textCommands = new ArrayList<>();
    private final List<CallbackCommand> callbackCommands = new ArrayList<>();
    private PostgreSQLConnection DBConnection;

    public CommandManager(TelegramClient telegramClient) {
        DBConnection = new PostgreSQLConnection(System.getenv("DB_URL"),
                                                System.getenv("DB_USER"), 
                                                System.getenv("DB_PASSWORD"));
        registerCommand(new StartCommand(telegramClient));
        registerCommand(new HelpCommand(telegramClient, textCommands));
        registerCommand(new CreateCartCommand(telegramClient));
        // ...
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

        // Обработка неизвестной команды
    }
    
    private void processCallbackCommand(Update update) throws TelegramApiException {
        for (CallbackCommand command : callbackCommands) {
            if (command.shouldProcess(update)) {
                command.execute(update);
                return;
            }
        }
    }
    
    private void processUnknownUpdate(Update update) {
        // Логирование неподдерживаемых типов сообщений
    }
}
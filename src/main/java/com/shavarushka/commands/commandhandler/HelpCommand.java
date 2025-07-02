package com.shavarushka.commands.commandhandler;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotCommand;
import com.shavarushka.commands.intr.TextCommand;

public class HelpCommand extends AbstractTextCommand {
    private final List<TextCommand> textCommands;

    public HelpCommand(TelegramClient telegramClient, List<TextCommand> commands) {
        super(telegramClient);
        textCommands = commands;
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Помощь по командам";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String helpMessage = BotCommand.escapeMarkdownV2("Мини-справка по командам бота:\n\n");
        for (TextCommand command : textCommands) {
            if (!command.getCommand().equals("") || !command.getDescription().equals(""))
                helpMessage += BotCommand.escapeMarkdownV2("  " + command.getCommand() + " - " + command.getDescription() + "\n");
        }
        sendMessage(chatId, helpMessage);
    }
}

package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotCommand;
import com.shavarushka.commands.intr.BotState;

public class HelpCommand extends AbstractTextCommand {
    private final Map<String, BotCommand> commands;

    public HelpCommand(TelegramClient telegramClient, Map<Long, BotState> userStates, Map<String, BotCommand> commands) {
        super(telegramClient, userStates);
        this.commands = commands;
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
        String message = escapeMarkdownV2("Мини-справка по командам бота:\n\n");
        for (BotCommand command : commands.values()) {
            if (command instanceof AbstractTextCommand textCommand) {
                if (!textCommand.getCommand().equals("") || !textCommand.getDescription().equals(""))
                    message += escapeMarkdownV2("  " + textCommand.getCommand() + " - " + textCommand.getDescription() + "\n");
            }
        }
        sendMessage(chatId, message);
    }
}

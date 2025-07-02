package com.shavarushka.commands.commandhandler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand extends AbstractTextCommand {
    public StartCommand(TelegramClient telegramClient) {
        super(telegramClient);
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Приветствие!";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        sendMessage(chatId, "Добро пожаловать " + userName + "\\!");
    }
}
package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotState;

public class StartCommand extends AbstractTextCommand {
    public StartCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
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
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        sendMessage(chatId, "Привет *" + userName + "*\\!");
    }
}
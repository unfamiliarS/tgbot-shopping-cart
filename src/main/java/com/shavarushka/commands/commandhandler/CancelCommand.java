package com.shavarushka.commands.commandhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotCommand;
import com.shavarushka.commands.intr.BotState;

public class CancelCommand extends AbstractTextCommand {
    private final Map<Long, BotState> userStates;

    public CancelCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient);
        this.userStates = userStates;
    }

    @Override
    public String getCommand() {
        return "/cancel";
    }

    @Override
    public String getDescription() {
        return "Состояние сброшено, все команды были отмененны";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        // clear bot state for chat
        userStates.remove(chatId);
        String message = BotCommand.escapeMarkdownV2(getDescription());
        sendMessage(chatId, message);
    }

}

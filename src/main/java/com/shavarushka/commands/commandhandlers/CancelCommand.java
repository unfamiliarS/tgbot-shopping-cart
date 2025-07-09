package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class CancelCommand extends AbstractTextCommand {
    public CancelCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
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
        Long chatId = update.getMessage().getChatId();
        // clear bot state for chat
        userStates.remove(chatId);
        String message = MessageSender.escapeMarkdownV2(getDescription());
        sender.sendMessage(chatId, message);
    }

}

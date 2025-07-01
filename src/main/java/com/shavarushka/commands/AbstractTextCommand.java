package com.shavarushka.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.TextCommand;

public abstract class AbstractTextCommand implements TextCommand {
    protected final TelegramClient telegramClient;
    
    public AbstractTextCommand(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessage(Long chatId, String text) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(text)
                                    .build());
    }

    public void sendMessage(Long chatId, ReplyKeyboard keyboard) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .replyMarkup(keyboard)
                                    .build());
    }
}
package com.shavarushka.commands.intr;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotCommand {
    void execute(Update update) throws TelegramApiException;
    boolean shouldProcess(Update update);
    void sendMessage(Long chatId, String text) throws TelegramApiException;
    void editMessage(Long chatId, Integer messageId, String text) throws TelegramApiException;
}
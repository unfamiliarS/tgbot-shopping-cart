package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotCommand {
    String getCommand();
    boolean shouldProcess(Update update);
    void execute(Update update) throws TelegramApiException;
}
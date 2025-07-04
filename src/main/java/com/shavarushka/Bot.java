package com.shavarushka;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.CommandManager;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private CommandManager commandManager;

    public Bot(String token) {
        TelegramClient telegramClient = new OkHttpTelegramClient(token);
        commandManager = new CommandManager(telegramClient);
    }

    @Override
    public void consume(Update update) {
        try {
            commandManager.processUpdate(update);
        } catch (TelegramApiRequestException e) {
        // TODO: Add button handling with the same value as "selected cart"
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

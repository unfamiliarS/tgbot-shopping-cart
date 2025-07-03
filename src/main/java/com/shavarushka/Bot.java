package com.shavarushka;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private CommandManager commandManager;

    public Bot(String token) {
        telegramClient = new OkHttpTelegramClient(token);
        commandManager = new CommandManager(telegramClient);
    }

    @Override
    public void consume(Update update) {
        try {
            commandManager.processUpdate(update);
        } catch (TelegramApiRequestException e) {
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

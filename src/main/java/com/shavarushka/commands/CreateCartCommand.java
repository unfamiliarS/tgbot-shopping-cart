package com.shavarushka.commands;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CreateCartCommand extends AbstractTextCommand {

    public CreateCartCommand(TelegramClient telegramClient) {
        super(telegramClient);
    }

    @Override
    public String getCommand() {
        return "/createNewCart";
    }

    @Override
    public String getDescription() {
        return "Создание новой корзины";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Введите название корзины");
    }

}

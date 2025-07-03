package com.shavarushka.commands.callbackhandler;

import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.intr.BotState;
import com.shavarushka.commands.intr.CallbackCommand;

public abstract class AbstractCallbackCommand implements CallbackCommand {
    protected final TelegramClient telegramClient;
    protected final Map<Long, BotState> userStates;
    
    public AbstractCallbackCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        this.telegramClient = telegramClient;
        this.userStates = userStates;
    }
    
    // should override if need to check BotState
    @Override
    public boolean shouldProcess(Update update) {
        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callback = update.getCallbackQuery().getData();
            return userStates.containsKey(chatId) &&
                   callback.startsWith(getCallbackPattern().strip());
        }
        return false;
    }

    public void sendMessage(Long chatId, String text) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(text)
                                    .parseMode(ParseMode.MARKDOWNV2)
                                    .build());
    }

    public void sendMessage(Long chatId, ReplyKeyboard keyboard) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .replyMarkup(keyboard)
                                    .build());
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(text)
                                    .parseMode(ParseMode.MARKDOWNV2)
                                    .replyMarkup(keyboard)
                                    .build());
    }
}
package com.shavarushka.commands.interfaces;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageSender {
    private final TelegramClient telegramClient;
    
    public MessageSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient; 
    }

    public void sendMessage(Long chatId, String text) throws TelegramApiException {
        telegramClient.execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(text)
                                    .parseMode(ParseMode.MARKDOWNV2)
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

    public void editMessage(Long chatId, Integer messageId, String text) throws TelegramApiException {
        telegramClient.execute(EditMessageText.builder()
                                    .chatId(chatId)
                                    .messageId(messageId)
                                    .text(text)
                                    .parseMode(ParseMode.MARKDOWNV2)
                                    .build());
    }

    public void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        telegramClient.execute(EditMessageText.builder()
                                    .chatId(chatId)
                                    .messageId(messageId)
                                    .text(text)
                                    .parseMode(ParseMode.MARKDOWNV2)
                                    .replyMarkup(keyboard) 
                                    .build());
    }

    static public String escapeMarkdownV2(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}

package com.shavarushka.commands;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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

    public void sendMessage(Long chatId, String text, boolean withMarkdown) throws TelegramApiException {
        var message = SendMessage.builder()
                        .chatId(chatId)
                        .text(text);
        if (withMarkdown) {
            message.parseMode(ParseMode.MARKDOWNV2);
        }

        telegramClient.execute(message.build());
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboard keyboard, boolean withMarkdown) throws TelegramApiException {
        var message = SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .replyMarkup(keyboard);
        if (withMarkdown) {
            message.parseMode(ParseMode.MARKDOWNV2);
        }

        telegramClient.execute(message.build());
    }

    public void sendMessage(Long chatId, ReplyKeyboard keyboard, boolean withMarkdown) throws TelegramApiException {
        var message = SendMessage.builder()
                        .chatId(chatId)
                        .replyMarkup(keyboard);
        if (withMarkdown) {
            message.parseMode(ParseMode.MARKDOWNV2);
        }

        telegramClient.execute(message.build());
    }

    public void editMessage(Long chatId, Integer messageId, String text, boolean withMarkdown) throws TelegramApiException {
        var message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text);
        if (withMarkdown) {
            message.parseMode(ParseMode.MARKDOWNV2);
        }

        telegramClient.execute(message.build());
    }

    public void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard, boolean withMarkdown) throws TelegramApiException {
        var message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard);
        if (withMarkdown) {
            message.parseMode(ParseMode.MARKDOWNV2);
        }

        telegramClient.execute(message.build());
    }

    public void deleteMessage(Long chatId, Integer messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage(chatId.toString(), messageId);
        telegramClient.execute(deleteMessage);
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

package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.commandhandlers.interfaces.AbstractTextCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class AddProductCommand extends AbstractTextCommand {

    public AddProductCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    @Override
    public String getCommand() {
        return "Добавить товар";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean shouldProcess(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return false;

        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        String regexURL = "^https://www\\.wildberries\\.ru/catalog/\\d+/detail\\.aspx.*$";
        return !userStates.containsKey(chatId) &&
                message.matches(regexURL);
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String message = update.getMessage().getText();
        sender.sendMessage(chatId, message, false);

    }

}

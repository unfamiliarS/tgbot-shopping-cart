package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class StartCommand extends AbstractTextCommand {
    public StartCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String getDescription() {
        return "Приветствие!";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getFirstName();
        sender.sendMessage(chatId, "Привет *" + userName + "*\\!");
    }
}
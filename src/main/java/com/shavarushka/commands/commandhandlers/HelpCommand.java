package com.shavarushka.commands.commandhandlers;

import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.shavarushka.commands.interfaces.BotCommand;
import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public class HelpCommand extends AbstractTextCommand {
    private final Map<String, BotCommand> commands;

    public HelpCommand(MessageSender sender, Map<Long, BotState> userStates, Map<String, BotCommand> commands) {
        super(sender, userStates);
        this.commands = commands;
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Помощь по командам";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        String message = MessageSender.escapeMarkdownV2("Мини-справка по командам бота:\n\n");
        for (BotCommand command : commands.values()) {
            if (command instanceof AbstractTextCommand textCommand) {
                if (textCommand.getCommand().startsWith("/") || !textCommand.getDescription().equals(""))
                    message += MessageSender.escapeMarkdownV2(
                                            "  " + 
                                            textCommand.getCommand() + 
                                            " - " + 
                                            textCommand.getDescription() + 
                                            "\n");
            }
        }
        sender.sendMessage(chatId, message);
    }
}

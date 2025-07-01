package com.shavarushka.commands.intr;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TextCommand extends BotCommand {
    String getCommand();
    String getDescription();
    
    @Override
    default boolean shouldProcess(Update update) {
        return update.hasMessage() && 
               update.getMessage().hasText() &&
               update.getMessage().getText().matches(getCommand());
    }
}

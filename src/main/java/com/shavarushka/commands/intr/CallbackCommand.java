package com.shavarushka.commands.intr;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackCommand extends BotCommand {
    String getCallbackPattern();
    
    @Override
    default boolean shouldProcess(Update update) {
        return update.hasCallbackQuery() &&
               update.getCallbackQuery().getData().startsWith(getCallbackPattern().strip());
    }
}

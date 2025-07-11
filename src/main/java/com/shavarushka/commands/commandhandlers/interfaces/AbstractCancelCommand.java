package com.shavarushka.commands.commandhandlers.interfaces;

import java.util.Map;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;

public abstract class AbstractCancelCommand extends AbstractTextCommand  {
    public AbstractCancelCommand(MessageSender sender, Map<Long, BotState> userStates) {
        super(sender, userStates);
    }
}

package com.shavarushka.commands.callbackhandlers.interfaces;

import java.util.List;
import java.util.Map;

import com.shavarushka.commands.interfaces.BotState;
import com.shavarushka.commands.interfaces.MessageSender;
import com.shavarushka.commands.keyboard.CartSelectionListener;

public abstract class SelectedCartNotifier extends AbstractCallbackCommand {
    private final List<CartSelectionListener> cartSelectionListeners;

    public SelectedCartNotifier(MessageSender sender, Map<Long, BotState> userStates, List<CartSelectionListener> listeners) {
        super(sender, userStates);
        cartSelectionListeners = listeners;
    }

    public void addCartSelectionListener(CartSelectionListener listener) {
        cartSelectionListeners.add(listener);
    }
    
    public void removeCartSelectionListener(CartSelectionListener listener) {
        cartSelectionListeners.remove(listener);
    }

    protected void notifyCartSelectionListeners(Long userId, Long cartId) {
        for (CartSelectionListener listener : cartSelectionListeners) {
            listener.onCartSelected(userId, cartId);
        }
    }
}

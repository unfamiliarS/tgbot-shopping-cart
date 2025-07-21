package com.shavarushka.commands.commandhandlers.interfaces;

import java.util.List;
import java.util.Map;

import com.shavarushka.commands.BotState;
import com.shavarushka.commands.MessageSender;
import com.shavarushka.commands.interfaces.SelectedCartNotifier;
import com.shavarushka.commands.keyboard.CartSelectionListener;
import com.shavarushka.database.SQLiteConnection;

public abstract class SelectedCartNotifierCommand extends AbstractTextCommand implements SelectedCartNotifier {
    private final List<CartSelectionListener> cartSelectionListeners;

    public SelectedCartNotifierCommand(MessageSender sender, Map<Long, BotState> userStates,
                                    SQLiteConnection connection, List<CartSelectionListener> listeners) {
        super(sender, userStates, connection);
        cartSelectionListeners = listeners;
    }

    public void addCartSelectionListener(CartSelectionListener listener) {
        cartSelectionListeners.add(listener);
    }
    
    public void removeCartSelectionListener(CartSelectionListener listener) {
        cartSelectionListeners.remove(listener);
    }

    public void notifyCartSelectionListeners(Long userId, Long cartId) {
        for (CartSelectionListener listener : cartSelectionListeners) {
            listener.onCartSelected(userId, cartId);
        }
    }
}

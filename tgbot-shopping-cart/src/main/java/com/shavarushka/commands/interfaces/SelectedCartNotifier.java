package com.shavarushka.commands.interfaces;

import com.shavarushka.commands.keyboard.CartSelectionListener;

public interface SelectedCartNotifier {
    void addCartSelectionListener(CartSelectionListener listener);
    void removeCartSelectionListener(CartSelectionListener listener);
    void notifyCartSelectionListeners(Long userId, Long cartId);
}

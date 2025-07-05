package com.shavarushka.commands.commandhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.commands.KeyboardsFabrics;
import com.shavarushka.commands.callbackhandlers.AbstractCallbackCommand;
import com.shavarushka.commands.interfaces.BotState;

public class MyCartCommand extends AbstractTextCommand {
    private List<String> cartNames = new ArrayList<>();
    private String selectedCart;

    public MyCartCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
        cartNames.addAll(List.of("Корзина Хорошика", "Корзина Поняшика", "Корзина Фуры"));
        selectedCart = cartNames.getFirst();
    }

    @Override
    public String getCommand() {
        return "/mycarts";
    }

    @Override
    public String getDescription() {
        return "Настройка твоих корзин";
    }

    @Override
    public void execute(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        String message;
        if (cartNames.isEmpty()) {
            message = escapeMarkdownV2("У вас нет ни одной корзины:( \n/createnewcart чтобы создать");
            sendMessage(chatId, message);
            return;
        }
        message = "Ваши корзины:";
        Map<String, String> buttons = new HashMap<>();
        for (String cart : cartNames) {
            String cartName = cart.equals(selectedCart) ? "✅ " + cart : cart;
            buttons.put(cartName, "/setcart_" + cart);
        }
        InlineKeyboardMarkup keyboard = KeyboardsFabrics.createInlineKeyboard(
                        buttons,
                        1);
        sendMessage(chatId, message, keyboard);
    }

    public class SetCartCallback extends AbstractCallbackCommand {

        public SetCartCallback(TelegramClient telegramClient, Map<Long, BotState> userStates) {
            super(telegramClient, userStates);
        }

        @Override
        public String getCallbackPattern() {
            return "/setcart_";
        }

        @Override
        public void execute(Update update) throws TelegramApiException {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            selectedCart = update.getCallbackQuery().getData().substring(getCallbackPattern().length());
            String message;
            if (cartNames.isEmpty()) {
                message = escapeMarkdownV2("У вас нет ни одной корзины:( \n/createnewcart чтобы создать");
                sendMessage(chatId, message);
                return;
            }
            message = "Ваши корзины:";
            Map<String, String> buttons = new HashMap<>();
            for (String cart : cartNames) {
                buttons.put(cart, "/setcart_" + cart);
                if (cart.equals(selectedCart)) {
                    buttons.remove(selectedCart);
                    buttons.put("✅ " + cart, "/setcart_" + cart);
                }
            }
            InlineKeyboardMarkup keyboard = KeyboardsFabrics.createInlineKeyboard(
                            buttons,
                            1);
            editMessage(chatId, messageId, message, keyboard);
        }
    }
}

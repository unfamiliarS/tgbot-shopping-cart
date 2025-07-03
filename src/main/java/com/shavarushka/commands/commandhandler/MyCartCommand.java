package com.shavarushka.commands.commandhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.shavarushka.KeyboardsFabrics;
import com.shavarushka.commands.intr.BotCommand;
import com.shavarushka.commands.intr.BotState;

public class MyCartCommand extends AbstractTextCommand {
    List<String> cartNames = new ArrayList<>();
    String selectedCart;

    public MyCartCommand(TelegramClient telegramClient, Map<Long, BotState> userStates) {
        super(telegramClient, userStates);
        cartNames.addAll(List.of("Корзина моя", "Корзина твоя", "Чья-то корзина"));
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
        long chatId = update.getMessage().getChatId();
        String message;
        if (cartNames.isEmpty()) {
            message = BotCommand.escapeMarkdownV2("У вас нет ни одной корзины:( \n/createnewcart чтобы создать");
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
        ReplyKeyboard keyboard = KeyboardsFabrics.createInlineKeyboard(
                        buttons,
                        1);
        sendMessage(chatId, message, keyboard);
    }

    public class changeCart {
        
    }
}

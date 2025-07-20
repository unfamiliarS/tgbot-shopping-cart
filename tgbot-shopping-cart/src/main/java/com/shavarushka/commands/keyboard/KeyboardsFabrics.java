package com.shavarushka.commands.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardsFabrics {
    public static <T extends ReplyKeyboard> T createKeyboard(Map<String, String> buttons, int buttonsPerRow, Class<T> keyboardType) {
        if (keyboardType.equals(InlineKeyboardMarkup.class)) {
            return keyboardType.cast(createInlineKeyboard(buttons, buttonsPerRow));
        } else if (keyboardType.equals(ReplyKeyboardMarkup.class)) {
            return keyboardType.cast(createReplyKeyboard(buttons, buttonsPerRow));
        } else {
            throw new IllegalArgumentException("Unsupported keyboard type");
        }
    }

    private static InlineKeyboardMarkup createInlineKeyboard(Map<String, String> buttons, int buttonsPerRow) {
        if (buttonsPerRow <= 0)
            throw new IndexOutOfBoundsException("Incorect buttons per row number");

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();
        
        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            currentRow.add(InlineKeyboardButton
                            .builder()
                            .text(entry.getValue())
                            .callbackData(entry.getKey())
                            .build()
            );
            
            if (currentRow.size() >= buttonsPerRow) {
                keyboardRows.add(currentRow);
                currentRow = new InlineKeyboardRow();
            }
        }
        
        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboardRows)
                .build();
    }

    private static ReplyKeyboardMarkup createReplyKeyboard(Map<String, String> buttons, int buttonsPerRow) {
        if (buttonsPerRow <= 0) {
            throw new IndexOutOfBoundsException("Incorrect buttons per row number");
        }

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            currentRow.add(KeyboardButton
                        .builder()
                        .text(entry.getValue())
                        .build()
            );

            if (currentRow.size() >= buttonsPerRow) {
                keyboardRows.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboardRows.add(currentRow);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(true)
                .build();
    }
}

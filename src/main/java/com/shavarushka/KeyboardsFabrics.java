package com.shavarushka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

public class KeyboardsFabrics {

    static public InlineKeyboardMarkup createInlineKeyboard(Map<String, String> buttons, int buttonsPerRow) {
        if (buttonsPerRow <= 0)
            throw new IndexOutOfBoundsException("Incorect buttons per row number");

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        InlineKeyboardRow currentRow = new InlineKeyboardRow();
        
        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            currentRow.add(InlineKeyboardButton
                            .builder()
                            .text(entry.getKey())
                            .callbackData(entry.getValue())
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
}

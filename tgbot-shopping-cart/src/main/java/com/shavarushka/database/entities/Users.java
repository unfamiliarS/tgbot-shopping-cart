package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Users(Long userId, Long chatId, String firstname, String username, Long selectedCartId, Timestamp registrationTime) {
    
    public Users(Long userId, Long chatId, String firstname, String username) {
        this(userId, chatId, firstname, username, null, null);
    }
    
}
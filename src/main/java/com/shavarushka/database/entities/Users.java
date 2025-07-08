package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Users(Long userId, String username, Long selectedCartId, Timestamp registrationTime) {}
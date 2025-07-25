package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record ShoppingCarts(
    Long cartId,
    String cartName,
    Timestamp creationTime
) {}
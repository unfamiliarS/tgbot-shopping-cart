package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Categories(Long categoryId, Long assignedCartId, String categoryName, Timestamp creationTime) {

    public Categories(Long assignedCartId, String categoryName) {
        this(null, assignedCartId, categoryName, null);
    }


}

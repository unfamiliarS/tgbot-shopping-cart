package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Categories(Long categoryId, Long assignedCartId, String categoryName, Timestamp creationTime) {

    public static final String DEFAULT_CATEGORY_NAME = "Прочее";
    
    public Categories(Long assignedCartId, String categoryName) {
        this(null, assignedCartId, categoryName, null);
    }

}

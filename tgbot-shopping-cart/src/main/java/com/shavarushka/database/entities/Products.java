package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Products(
    Long productId,
    String fullURL,
    Long assignedCategoryId,
    String productName,             // null for now
    Integer productPrice,           // null for now
    Boolean productPurchaseStatus,
    Timestamp addingTime
) {
    public String productPurchaseStatusAsString() {
        return productPurchaseStatus() ? "💚" : "💛";
    }
}

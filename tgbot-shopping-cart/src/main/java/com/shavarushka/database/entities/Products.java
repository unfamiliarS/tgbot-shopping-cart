package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Products(Long productId, String fullURL, Long assignedCategoryId, String productName, Integer productPrice, Boolean productPurchaseStatus, Timestamp addingTime) {

    public Products(String fullURL, Long assignedCategoryId) {
        this(null, fullURL, assignedCategoryId, null, null, false, null);
    }

    public String productPurchaseStatusAsString() {
        return productPurchaseStatus() ? "💚" : "🤍";
    }

}

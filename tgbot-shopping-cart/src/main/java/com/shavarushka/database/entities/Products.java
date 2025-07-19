package com.shavarushka.database.entities;

import java.sql.Timestamp;

public record Products(Long productId, String fullURL, Long assignedCategoryId, String productName, Integer productPrice, Timestamp addingTime) {}

package com.shavarushka.database.entities;

public record Settings(
    Long settingId,
    Boolean listAlreadyPurchased,
    Boolean notifyAboutProducts,    // null for now
    Boolean notifyAboutInviting
) {}

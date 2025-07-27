package com.shavarushka.database.entities;

public record Settings(Long settingId, Boolean listAlreadyPurchased, Boolean notifyAboutProducts, Boolean notifyAboutInviting) {
    
    public Settings(Long settingId) {
        this(settingId, true, true, true);
    }
    
}

package com.shavarushka;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String botToken = System.getenv("SHOPPING_CART_BOT_TOKEN");
        try (var botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new ShopingCartBot(botToken));
            System.out.println("SharedShoppingCartBot successfully started");
            // Ensure this prcess wait forever
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
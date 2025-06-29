package com.shavarushka;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

final public class Main {
    
    private static final String botToken = System.getenv("SHOPPING_CART_BOT_TOKEN");
    private static final ShopingCartBot bot = new ShopingCartBot(botToken);

    public static void main(String[] args) {
        try (var botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, bot);
            System.out.println("SharedShoppingCartBot successfully started");
            // Ensure this prcess wait forever
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.shavarushka;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

final public class Main {
    
    private static final String botToken;
    private static final Bot bot;

    static {
        botToken = System.getenv("SHOPPING_CART_BOT_TOKEN");
        bot = new Bot(botToken);
    }

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
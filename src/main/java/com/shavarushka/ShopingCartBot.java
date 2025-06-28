package com.shavarushka;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ShopingCartBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private PostgreSQLConnection DBConnection;

    public ShopingCartBot(String token) {
        telegramClient = new OkHttpTelegramClient(token);
        DBConnection = new PostgreSQLConnection(System.getenv("DB_URL"),
                                                System.getenv("DB_USER"), 
                                                System.getenv("DB_PASSWORD"));
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // set default variable
            String updateMessage = update.getMessage().getText().toLowerCase();
            long chatID = update.getMessage().getChatId();
            User user = update.getMessage().getFrom();
            long userID = user.getId();

            SendMessage messageToSend = switch(updateMessage.strip()) {
                case "/start" -> {
                    // Проверяем, есть ли пользователь, отправивший сообщение, в бд
                    // Если да, то смотрим к каким корзинам он уже привязан и выводим их ему на выбор, чтобы он в них вернулся
                    // Или же, если он не захочет, то пусть создаёт новую корзину
                    if (!DBConnection.userExists(userID)) {
                        System.out.println("Добавление нового пользователя: " + userID);
                        if (DBConnection.addUser(userID, user.getFirstName(), user.getLastName())) {
                            System.out.println(userID + ": " + user.getFirstName() + " " + user.getLastName() + " добавлен!");
                        }
                    } else {
                        System.out.println("Пользователь " + userID + " уже есть в бд");
                    }
                    
                    try (ResultSet resultFromQuery = DBConnection.getUserCarts(userID)) {
                        if (!resultFromQuery.isBeforeFirst()) {
                            // ResultSet пуст, пользователь не связан ни с одной из корзин
                            yield SendMessage.builder()
                                    .chatId(chatID)
                                    .text("Вы не связаны ни с одной корзиной. Хотите создать новую?\n"
                                        + "Введите комманду /createshoppingcart_cartn <название вашей корзины>")
                                    .build();
                        } else {
                            // ResultSet не пуст, выводим список существующих корзин
                            String cartsList = getUsersCarts(resultFromQuery);
                            yield SendMessage.builder()
                                    .chatId(chatID)
                                    .text(cartsList)
                                    .build();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    yield SendMessage
                         .builder()
                         .chatId(chatID)
                         .text("Start command.\nWellcome " + update.getMessage().getFrom().getFirstName() + "!")
                         .build();
                // После создания, даём выбор, кого он хочет пригласить в неё. 
                // Тому, кого он, выберет придёт уведомление от бота, с двумя кнопками, принять или отказаться от присоединения
                // Все эти действия сопровождаются взаимодействием с бд postgresql
                } case "/createshoppingcart" -> {
                    yield SendMessage.builder()
                        .chatId(chatID)
                        .text("Отлично! Введите название вашей корзины")
                        .build();
                } case "/listcarts" -> {
                    try (ResultSet resultFromQuery = DBConnection.getUserCarts(userID)) {
                        if (!resultFromQuery.isBeforeFirst()) {
                            // ResultSet пуст, пользователь не связан ни с одной из корзин
                            yield SendMessage.builder()
                                    .chatId(chatID)
                                    .text("Вы не связаны ни с одной корзиной. Хотите создать новую?")
                                    .build();
                        } else {
                            // ResultSet не пуст, выводим список существующих корзин
                            String cartsList = getUsersCarts(resultFromQuery);
                            yield SendMessage.builder()
                                    .chatId(chatID)
                                    .text(cartsList)
                                    .build();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        yield null;
                    }
                } default -> {
                    if (updateMessage.startsWith("/createshoppingcart_cartn")) {
                        String cartName = updateMessage.substring("/createShoppingCart_cartn".length()).strip();
                        if (DBConnection.addShoppingCart(userID, cartName)) {
                            try (ResultSet resultFromQuery = DBConnection.getUserCarts(userID)) {
                                String cartsList = getUsersCarts(resultFromQuery);
                                yield SendMessage.builder()
                                    .chatId(chatID)
                                    .text("Корзина " + cartName + " успешно создана!\n" + cartsList)
                                    .build();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    yield SendMessage
                         .builder()
                         .chatId(chatID)
                         .text("Unknown command")
                         .build();
                }
            };

            try {
                telegramClient.execute(messageToSend);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUsersCarts(ResultSet resultFromQuery) throws SQLException {
        StringBuffer cartsList = new StringBuffer("Ваши корзины:\n");
        while (resultFromQuery.next()) {
            String cartId = resultFromQuery.getString("cart_id");
            String cartName = resultFromQuery.getString("cart_name");
            cartsList.append("ID: ").append(cartId).append(", Название: ").append(cartName).append("\n");
        }
        return cartsList.toString();
    }
}

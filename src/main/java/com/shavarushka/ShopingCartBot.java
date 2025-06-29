package com.shavarushka;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
        SendMessage messageToSend = null;
        if (update.hasMessage() && update.getMessage().hasText()) {
            // set default variable
            String updateMessage = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();
            User user = update.getMessage().getFrom();
            long userID = user.getId();            
            
            // this is a command handler block
            messageToSend = switch(updateMessage.strip()) {
                case "/start" -> {
                    // Проверяем, есть ли пользователь, отправивший сообщение, в бд
                    // Если да, то смотрим к каким корзинам он уже привязан и выводим их ему на выбор, чтобы он в них вернулся
                    // Или же, если он не захочет, то пусть создаёт новую корзину
                    yield startCommandHandler(update);

                // После создания, даём выбор, кого он хочет пригласить в неё. 
                // Тому, кого он, выберет придёт уведомление от бота, с двумя кнопками, принять или отказаться от присоединения
                // Все эти действия сопровождаются взаимодействием с бд postgresql
                } case "/listCarts" -> {
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
                    if (updateMessage.startsWith("/createShoppingCart_cartn")) {
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
        // this is a callback queries handler block
        } else if (update.hasCallbackQuery()) {
            // set default variable
            String queryMessage = update.getCallbackQuery().getData();
            long chatID = update.getCallbackQuery().getMessage().getChatId();

            if (queryMessage.equals("/createShoppingCart")) {
                messageToSend = SendMessage.builder()
                        .chatId(chatID)
                        .text("Отлично! Введите название вашей корзины")
                        .build();
                // ...
            } else if (queryMessage.startsWith("/selectCart_")) {
                selectCartCallbackhandler(queryMessage, chatID);
            }
        }
        try {
            telegramClient.execute(messageToSend);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // start of startCommandHandler methods set
    private SendMessage startCommandHandler(Update update) {
        // set vars
        long userID = update.getMessage().getFrom().getId();
        User user = update.getMessage().getFrom();
        long chatID = update.getMessage().getChatId();
        long selectCartID = -1;

        // add new user
        if (!DBConnection.userExists(userID)) {
            System.out.println("Добавление нового пользователя: " + userID);
            if (DBConnection.addUser(userID, user.getFirstName(), user.getLastName())) {
                System.out.println(userID + ": " + user.getFirstName() + " " + user.getLastName() + " добавлен!");
            }
        }
        // get shopping cart for user
        if ((selectCartID = DBConnection.getSelectedCartForUser(userID)) != -1) {
            if (DBConnection.updateSelectedCartForUser(userID, selectCartID)) {
                return SendMessage.builder()
                    .chatId(chatID)
                    .text("Добро пожаловать в " + DBConnection.getCartByID(selectCartID))
                    .build();
            }
        }
        // if user don't have linked cart
        ResultSet userCarts = DBConnection.getUserCarts(userID);
        try {
            if (!userCarts.isBeforeFirst()) {
                return SendMessage.builder()
                        .chatId(chatID)
                        .text("У вас нет ни одной корзины. Хотите создать новую?")
                        .replyMarkup(KeyboardsFabrics.createInlineKeyboard(
                                        Map.of("Создать корзину", "/createShoppingCart"), //TODO: /createShoppingCart callback handler
                                        1))
                        .build();
            } else {
                Map<String, String> buttons = new HashMap<>();
                while (userCarts.next()) {
                    String cartId = userCarts.getString("cart_id");
                    String cartName = userCarts.getString("cart_name");
                    buttons.put(cartName, "/selectCart_" + cartId + "from_" + userID); 
                }
                return SendMessage.builder()
                        .chatId(chatID)
                        .text("Ваши корзины:")
                        .replyMarkup(KeyboardsFabrics.createInlineKeyboard(
                                buttons, 1))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return SendMessage.builder()
                    .chatId(String.valueOf(chatID))
                    .text("Произошла ошибка при получении списка корзин.")
                    .build();
        }
    }
    // end of startCommandHandler methods set

    // start of selectCartCallbackhandler methods set
    private SendMessage selectCartCallbackhandler(String queryMessage, long chatID) {
        String cartID = queryMessage.substring("/selectCart_".length(), queryMessage.lastIndexOf("from_"));
        String userID = queryMessage.substring(queryMessage.lastIndexOf("_")+1);
        if (DBConnection.updateSelectedCartForUser(Long.parseLong(userID), Long.parseLong(cartID))) {
            return SendMessage.builder()
                .chatId(chatID)
                .text("Добро пожаловать в " + DBConnection.getCartByID(Long.parseLong(cartID)))
                .build();
        }
        return SendMessage.builder()
                .chatId(String.valueOf(chatID))
                .text("Произошла ошибка при обновлении выбранной корзины.")
                .build();
    }
    // end of selectCartCallbackhandler methods set


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

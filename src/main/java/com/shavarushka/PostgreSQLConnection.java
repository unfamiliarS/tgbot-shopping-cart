package com.shavarushka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLConnection {

    private Connection connection;

    public PostgreSQLConnection(String url, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для проверки существования пользователя в базе данных
    public boolean userExists(long userId) {
        String query = "SELECT 1 FROM \"Users\" WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для добавления нового пользователя в базу данных
    public boolean addUser(long userId, String firstname, String lastname) {
        String query = "INSERT INTO \"Users\" (user_id, first_name, last_name) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, firstname);
            statement.setString(3, lastname);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для получения корзин пользователя
    public ResultSet getUserCarts(long userId) {
        String query = "SELECT sc.cart_id, sc.cart_name " +
                    "FROM \"Shoping_Carts\" sc " +
                    "JOIN \"Users_ShopingCarts\" usc ON sc.cart_id = usc.cart_id " +
                    "WHERE usc.user_id = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, userId);
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для добавления новой корзины в базу данных и получения её ID
    public boolean addShoppingCart(long userID, String cartName) {
        String query = "INSERT INTO \"Shoping_Carts\" (cart_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cartName);
            statement.executeUpdate();

            // Получаем сгенерированные ключи
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    addUserToShoppingCart(userID, generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating cart failed, no ID obtained.");
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для добавления связи между пользователем и корзиной в промежуточную таблицу
    private boolean addUserToShoppingCart(long userId, int cartId) {
        String query = "INSERT INTO \"Users_ShopingCarts\" (user_id, cart_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setInt(2, cartId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

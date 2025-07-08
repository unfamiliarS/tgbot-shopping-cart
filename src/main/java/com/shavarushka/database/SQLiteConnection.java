package com.shavarushka.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;

final public class SQLiteConnection {
    private Connection connection;

    public SQLiteConnection(String url) {
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Users getUserById(Long userId) {
        String query = "SELECT user_id, username, selected_cart, registration_time FROM users WHERE user_id = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return new Users(resultSet.getLong("user_id"),
                                resultSet.getString("username"), 
                                resultSet.getLong("selected_cart"), 
                                resultSet.getTimestamp("registration_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ShoppingCarts getShoppingCartById(Long cartId) {
        String query = "SELECT cart_id, cart_name, creation_time FROM shopping_carts WHERE cart_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return new ShoppingCarts(resultSet.getLong("cart_id"), 
                                        resultSet.getString("cart_name"), 
                                        resultSet.getTimestamp("creation_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addUser(Users user) {
        String query = user.selectedCartId() == null ? 
            "INSERT INTO users (user_id, username) VALUES (?, ?)" :
            "INSERT INTO users (user_id, username, selected_cart) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.userId());
            statement.setString(2, user.username());
            if (user.selectedCartId() != null)
                statement.setLong(3, user.selectedCartId());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addShoppingCart(ShoppingCarts cart, Users associatedUser) {
        String query = "INSERT INTO shopping_carts (cart_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cart.cartName());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long cartId = generatedKeys.getLong(1);
                    if (associatedUser.selectedCartId() == null) {
                        updateSelectedCartForUser(associatedUser, cartId);
                    }
                    addUserToShoppingCart(associatedUser, cartId);
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

    // add relationship between user and cart to intermediate table
    private boolean addUserToShoppingCart(Users user, Long cartId) {
        String query = "INSERT INTO users_shopping_carts (user_id, cart_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.userId());
            statement.setLong(2, cartId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSelectedCartForUser(Users user, Long cartId) {
        String query = "UPDATE users SET selected_cart = ? WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            statement.setLong(2, user.userId());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
}

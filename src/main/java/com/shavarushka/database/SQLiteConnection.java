package com.shavarushka.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

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
        String query = "SELECT user_id, chat_id, username, user_firstname, selected_cart, " + 
            "registration_time FROM users WHERE user_id = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return new Users(resultSet.getLong("user_id"),
                                resultSet.getLong("chat_id"),
                                resultSet.getString("user_firstname"),
                                resultSet.getString("username"), 
                                resultSet.getLong("selected_cart"), 
                                resultSet.getTimestamp("registration_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Users getUserByUsername(String username) {
        String query = "SELECT user_id, chat_id, username, user_firstname, selected_cart, " + 
            "registration_time FROM users WHERE username = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return new Users(resultSet.getLong("user_id"),
                                resultSet.getLong("chat_id"),
                                resultSet.getString("user_firstname"),
                                resultSet.getString("username"), 
                                resultSet.getLong("selected_cart"), 
                                resultSet.getTimestamp("registration_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ShoppingCarts getCartById(Long cartId) {
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
            "INSERT INTO users (user_id, chat_id, user_firstname, username) VALUES (?, ?, ?, ?)" :
            "INSERT INTO users (user_id, chat_id, user_firstname, username, selected_cart) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.userId());
            statement.setLong(2, user.chatId());
            statement.setString(3, user.firstname());
            statement.setString(4, user.username());
            if (user.selectedCartId() != null)
                statement.setLong(5, user.selectedCartId());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCart(ShoppingCarts cart, Users associatedUser) {
        String query = "INSERT INTO shopping_carts (cart_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cart.cartName());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long cartId = generatedKeys.getLong(1);
                    updateSelectedCartForUser(associatedUser.userId(), cartId);
                    addUserToCart(associatedUser.userId(), cartId);
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
    public boolean addUserToCart(Long userId, Long cartId) {
        String query = "INSERT OR IGNORE INTO users_shopping_carts (cart_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            statement.setLong(2, userId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSelectedCartForUser(Long userId, Long cartId) {
        String query = "UPDATE OR IGNORE users SET selected_cart = ? WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            statement.setLong(2, userId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<ShoppingCarts> getCartsAssignedToUser(Long userId) {
        String query = "SELECT sc.cart_id, sc.cart_name, sc.creation_time " +
                    "FROM shopping_carts sc " +
                    "JOIN users_shopping_carts usc ON sc.cart_id = usc.cart_id " +
                    "WHERE usc.user_id = ?";

        Set<ShoppingCarts> carts = new HashSet<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ShoppingCarts cart = new ShoppingCarts(
                    resultSet.getLong("cart_id"),
                    resultSet.getString("cart_name"),
                    resultSet.getTimestamp("creation_time")
                );
                carts.add(cart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carts;
    }

    public Set<Users> getUsersAssignedToCart(Long cartId) {
        String query = "SELECT u.user_id, u.chat_id, u.user_firstname, u.username, u.selected_cart, u.registration_time " +
                    "FROM users u " +
                    "JOIN users_shopping_carts usc ON u.user_id = usc.user_id " +
                    "WHERE usc.cart_id = ?";
        Set<Users> users = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Users user = new Users(resultSet.getLong("user_id"),
                                resultSet.getLong("chat_id"),
                                resultSet.getString("user_firstname"),
                                resultSet.getString("username"), 
                                resultSet.getLong("selected_cart"), 
                                resultSet.getTimestamp("registration_time"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
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

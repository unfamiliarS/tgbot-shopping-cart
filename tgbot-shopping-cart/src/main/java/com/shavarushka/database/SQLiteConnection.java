package com.shavarushka.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import com.shavarushka.database.entities.Categories;
import com.shavarushka.database.entities.Products;
import com.shavarushka.database.entities.Settings;
import com.shavarushka.database.entities.ShoppingCarts;
import com.shavarushka.database.entities.Users;
import com.shavarushka.database.interfaces.DBConnection;

final public class SQLiteConnection implements DBConnection {
    private Connection connection;

    public SQLiteConnection(String url) {
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
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

    // Read---------------------------------------------------------------------------------------------------

    public Users getUserById(Long userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new Users(rs.getLong("user_id") != 0 ? rs.getLong("user_id") : null,
                                rs.getLong("chat_id") != 0 ? rs.getLong("chat_id") : null,
                                rs.getString("user_firstname"),
                                rs.getString("username"), 
                                rs.getLong("selected_cart_id") != 0 ? rs.getLong("selected_cart_id") : null, 
                                rs.getTimestamp("registration_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Users getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new Users(rs.getLong("user_id") != 0 ? rs.getLong("user_id") : null,
                                rs.getLong("chat_id") != 0 ? rs.getLong("chat_id") : null,
                                rs.getString("user_firstname"),
                                rs.getString("username"), 
                                rs.getLong("selected_cart_id") != 0 ? rs.getLong("selected_cart_id") : null, 
                                rs.getTimestamp("registration_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ShoppingCarts getCartById(Long cartId) {
        String query = "SELECT * FROM shopping_carts WHERE cart_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new ShoppingCarts(rs.getLong("cart_id") != 0 ? rs.getLong("cart_id") : null, 
                                        rs.getString("cart_name"), 
                                        rs.getTimestamp("creation_time"));
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<ShoppingCarts> getCartsAssignedToUser(Long userId) {
        String query = "SELECT sc.* FROM shopping_carts sc " +
                    "JOIN users_shopping_carts usc ON sc.cart_id = usc.cart_id " +
                    "WHERE usc.user_id = ?";
        Set<ShoppingCarts> carts = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ShoppingCarts cart = new ShoppingCarts(
                    rs.getLong("cart_id") != 0 ? rs.getLong("cart_id") : null,
                    rs.getString("cart_name"),
                    rs.getTimestamp("creation_time")
                );
                carts.add(cart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carts;
    }

    public Set<Users> getUsersAssignedToCart(Long cartId) {
        String query = "SELECT u.* FROM users u " +
                    "JOIN users_shopping_carts usc ON u.user_id = usc.user_id " +
                    "WHERE usc.cart_id = ?";
        Set<Users> users = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Users user = new Users(
                                rs.getLong("user_id") != 0 ? rs.getLong("user_id") : null,
                                rs.getLong("chat_id") != 0 ? rs.getLong("chat_id") : null,
                                rs.getString("user_firstname"),
                                rs.getString("username"), 
                                rs.getLong("selected_cart_id") != 0 ? rs.getLong("selected_cart_id") : null, 
                                rs.getTimestamp("registration_time"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public Categories getCategoryById(Long categoryId) {
        String query = "SELECT * FROM categories WHERE category_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, categoryId);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new Categories(
                        rs.getLong("category_id") != 0 ? rs.getLong("category_id") : null,
                        rs.getLong("assigned_cart_id") != 0 ? rs.getLong("assigned_cart_id") : null,
                        rs.getString("category_name"), 
                        rs.getTimestamp("creation_time")
                );
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<Categories> getCategoriesByCartId(Long cartId) {
        String query = "SELECT * FROM categories WHERE assigned_cart_id = ? ORDER BY creation_time";
        
        Set<Categories> categories = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                categories.add(new Categories(
                                rs.getLong("category_id") != 0 ? rs.getLong("category_id") : null,
                                rs.getLong("assigned_cart_id") != 0 ? rs.getLong("assigned_cart_id") : null,
                                rs.getString("category_name"), 
                                rs.getTimestamp("creation_time"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Categories getCategoryByAssignedCartIdAndName(Long cartId, String categoryName) {
        String query = "SELECT * FROM categories " +
                    "WHERE assigned_cart_id = ? AND category_name = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            statement.setString(2, categoryName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Categories(
                    rs.getLong("category_id") != 0 ? rs.getLong("category_id") : null,
                    rs.getLong("assigned_cart_id") != 0 ? rs.getLong("assigned_cart_id") : null,
                    rs.getString("category_name"), 
                    rs.getTimestamp("creation_time"));
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Products getProductById(Long productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, productId);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new Products(
                    rs.getLong("product_id") != 0 ? rs.getLong("product_id") : null,
                    rs.getString("full_url"),
                    rs.getLong("assigned_category_id") != 0 ? rs.getLong("assigned_category_id") : null,
                    rs.getString("product_name"),
                    rs.getInt("product_price") != 0 ? rs.getInt("product_price") : null,
                    rs.getBoolean("product_purchase_status"),
                    rs.getTimestamp("adding_time")
                );
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // in cart url only unique
    public Products getProductByUrlAndCart(String url, Long cartId) {
        String query = "SELECT p.* FROM products p " +
                    "JOIN categories c ON p.assigned_category_id = c.category_id " +
                    "WHERE p.full_url = ? AND c.assigned_cart_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setString(1, url);
            statement.setLong(2, cartId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Products(
                    rs.getLong("product_id") != 0 ? rs.getLong("product_id") : null,
                    rs.getString("full_url"),
                    rs.getLong("assigned_category_id") != 0 ? rs.getLong("assigned_category_id") : null,
                    rs.getString("product_name"),
                    rs.getInt("product_price") != 0 ? rs.getInt("product_price") : null,
                    rs.getBoolean("product_purchase_status"),
                    rs.getTimestamp("adding_time")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<Products> getProductsByCategoryId(Long categoryId) {
        String query = "SELECT * FROM products WHERE assigned_category_id = ?";
        
        Set<Products> product = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, categoryId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                product.add(new Products(
                    rs.getLong("product_id") != 0 ? rs.getLong("product_id") : null,
                    rs.getString("full_url"),
                    rs.getLong("assigned_category_id") != 0 ? rs.getLong("assigned_category_id") : null,
                    rs.getString("product_name"),
                    rs.getInt("product_price") != 0 ? rs.getInt("product_price") : null,
                    rs.getBoolean("product_purchase_status"),
                    rs.getTimestamp("adding_time"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    public Settings getSettingsById(Long userId) {
        String query = "SELECT * FROM settings WHERE setting_id = ?";        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return new Settings(
                    rs.getLong("setting_id"),
                    rs.getBoolean("list_already_purchased"),
                    rs.getBoolean("notify_about_products"),
                    rs.getBoolean("notify_about_inviting")
                );
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create---------------------------------------------------------------------------------------------------

    public Long addUser(Users user) {
        String query = user.selectedCartId() == null ? 
            "INSERT INTO users (user_id, chat_id, user_firstname, username) VALUES (?, ?, ?, ?)" :
            "INSERT INTO users (user_id, chat_id, user_firstname, username, selected_cart_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.userId());
            statement.setLong(2, user.chatId());
            statement.setString(3, user.firstname());
            statement.setString(4, user.username());
            if (user.selectedCartId() != null)
                statement.setLong(5, user.selectedCartId());
            statement.executeUpdate();
            return user.userId();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long addCart(ShoppingCarts cart, Long associatedUserId) {
        String query = "INSERT INTO shopping_carts (cart_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cart.cartName());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long cartId = generatedKeys.getLong(1);
                    updateSelectedCartForUser(associatedUserId, cartId);
                    addUserToCartIntermediate(associatedUserId, cartId);
                    return cartId;
                } else {
                    throw new SQLException("Creating cart failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // add relationship between user and cart to intermediate table
    public boolean addUserToCartIntermediate(Long userId, Long cartId) {
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

    public Long addCategory(Categories category) {
        String query = "INSERT INTO categories (category_name, assigned_cart_id) VALUES (?, ?)";        
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.categoryName());
            statement.setLong(2, category.assignedCartId());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long addProduct(Products product) {
        String query = "INSERT INTO products (full_url, assigned_category_id, product_purchase_status) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, product.fullURL());
            statement.setLong(2, product.assignedCategoryId());
            statement.setBoolean(3, product.productPurchaseStatus());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long addSettings(Settings settings) {
        String query = "INSERT INTO settings (setting_id, list_already_purchased, notify_about_products, notify_about_inviting) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, settings.settingId());
            statement.setBoolean(2, settings.listAlreadyPurchased());
            statement.setBoolean(3, settings.notifyAboutProducts());
            statement.setBoolean(4, settings.notifyAboutInviting());
            statement.executeUpdate();
            return settings.settingId();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Update---------------------------------------------------------------------------------------------------

    public boolean updateSelectedCartForUser(Long userId, Long cartId) {
        String query = "UPDATE OR IGNORE users SET selected_cart_id = ? WHERE user_id = ?";
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

    public boolean updateCategoryForProduct(Long productId, Long categoryId) {
        String query = "UPDATE OR IGNORE products SET assigned_category_id = ? WHERE product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, categoryId);
            statement.setLong(2, productId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePurchaseStatusForProduct(Long productId, Boolean purchaseStatus) {
        String query = "UPDATE OR IGNORE products SET product_purchase_status = ? WHERE product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, purchaseStatus);
            statement.setLong(2, productId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateListAlreadyPurchasedSetting(Long settingId, Boolean settingVal) {
        return updateSetting(settingId, settingVal, "list_already_purchased");
    }

    public boolean updateNotifyAboutProductsSetting(Long settingId, Boolean settingVal) {
        return updateSetting(settingId, settingVal, "notify_about_products");
    }

    public boolean updateNotifyAboutInvitingSetting(Long settingId, Boolean settingVal) {
        return updateSetting(settingId, settingVal, "notify_about_inviting");
    }

    private boolean updateSetting(Long settingId, Boolean settingVal, String rowName) {
        String query = "UPDATE OR IGNORE settings SET " + rowName + " = ? WHERE setting_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, settingVal);
            statement.setLong(2, settingId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }        
    }

    // Delete---------------------------------------------------------------------------------------------------

    public boolean deleteCart(Long cartId) {
        String query = "DELETE FROM shopping_carts WHERE cart_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, cartId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCartFromIntermediate(Long userId, Long cartId) {
        String query = "DELETE FROM users_shopping_carts WHERE cart_id = ? AND user_id = ?";
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

    public boolean deleteCategory(Long categoryId) {
        String query = "DELETE FROM categories WHERE category_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, categoryId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(Long productId) {
        String query = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, productId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

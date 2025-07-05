package com.shavarushka.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.shavarushka.database.entities.ShoppingCart;
import com.shavarushka.database.entities.User;

import java.time.LocalDateTime;

public class DatabaseOperations {

    private final SessionFactory sessionFactory;

    public DatabaseOperations() {
        // Инициализация SessionFactory
        sessionFactory = new MetadataSources(
            new StandardServiceRegistryBuilder()
                .configure()
                .build()
        ).buildMetadata().buildSessionFactory();
    }

    // Метод для добавления новой корзины
    public void addShoppingCart(String cartName) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            ShoppingCart cart = new ShoppingCart(cartName);
            session.persist(cart);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Метод для получения корзины по ID
    public ShoppingCart getShoppingCartById(Long cartId) {
        Session session = sessionFactory.openSession();
        try {
            return session.find(ShoppingCart.class, cartId);
        } finally {
            session.close();
        }
    }

    // Метод для обновления корзины
    public void updateShoppingCart(Long cartId, String newCartName) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            ShoppingCart cart = session.find(ShoppingCart.class, cartId);
            if (cart != null) {
                cart.setCartName(newCartName);
                session.persist(cart);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Метод для удаления корзины
    public void deleteShoppingCart(Long cartId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            ShoppingCart cart = session.find(ShoppingCart.class, cartId);
            if (cart != null) {
                session.persist(cart);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Метод для добавления нового пользователя
    public void addUser(String firstName, String lastName, ShoppingCart selectedCart) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setSelectedCart(selectedCart);
            user.setRegistrationTime(LocalDateTime.now());
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Метод для добавления нового пользователя
    public void addUser(Long userId, String firstName, ShoppingCart selectedCart) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            User user = new User(userId, firstName, selectedCart);
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Метод для получения пользователя по ID
    public User getUserById(Long userId) {
        Session session = sessionFactory.openSession();
        try {
            return session.find(User.class, userId);
        } finally {
            session.close();
        }
    }

    public void close() {
        sessionFactory.close();
    }
}


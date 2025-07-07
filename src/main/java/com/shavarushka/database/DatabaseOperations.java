package com.shavarushka.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.shavarushka.database.entities.ShoppingCart;

import com.shavarushka.database.entities.User;

public class DatabaseOperations {

    private SessionFactory sessionFactory;

    public DatabaseOperations() {
        StandardServiceRegistry registry = 
            new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(ShoppingCart.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void addUser(User user) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addShoppingCart(ShoppingCart cart) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.persist(cart);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSelectedShoppingCartForUser(Long userId, ShoppingCart cart) {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            User user = getUserById(userId);
            user.setSelectedCart(cart);
            session.refresh(user);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserById(Long userId) {
        try (var session = sessionFactory.openSession()) {
            return session.find(User.class, userId);
        }
    }

    public ShoppingCart getShoppingCartById(Long cartId) {
        Session session = sessionFactory.openSession();
        try {
            return session.find(ShoppingCart.class, cartId);
        } finally {
            session.close();
        }
    }

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



    public void close() {
        sessionFactory.close();
    }
}


package com.shavarushka.database.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_first_name", nullable = false)
    private String firstName;

    @Column(name = "user_last_name")
    private String lastName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "selected_cart")
    private ShoppingCart selectedCart;
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "users_shopping_carts",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "cart_id")
    )
    private Set<ShoppingCart> carts;

    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime;

    public User() {}

    public User(Long userId, String firstName, ShoppingCart selectedCart) {
        this.firstName = firstName;
        this.selectedCart = selectedCart;
        this.carts = new HashSet<>();
        this.carts.add(selectedCart);
        this.registrationTime = LocalDateTime.now();
    }

    public User(String firstName, String lastName, ShoppingCart selectedCart) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.selectedCart = selectedCart;
        this.carts = new HashSet<>();
        this.carts.add(selectedCart);
        this.registrationTime = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ShoppingCart getSelectedCart() {
        return selectedCart;
    }

    public void setSelectedCart(ShoppingCart selectedCart) {
        this.selectedCart = selectedCart;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }
}
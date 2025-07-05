    package com.shavarushka.database.entities;

    import jakarta.persistence.CascadeType;
    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
    import jakarta.persistence.Table;

    import java.time.LocalDateTime;
import java.util.Set;

    @Entity
    @Table(name = "shopping_carts")
    public class ShoppingCart {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "cart_id")
        private Long cartId;

        @Column(name = "cart_name", nullable = false)
        private String cartName;

        @ManyToMany(mappedBy = "carts")
        private Set<User> users;

        @Column(name = "creation_time", nullable = false)
        private LocalDateTime creationTime;

        public ShoppingCart() {}

        public ShoppingCart(String cartName) {
            this.cartName = cartName;
            this.creationTime = LocalDateTime.now();
        }

        public Long getCartId() {
            return cartId;
        }

        public void setCartId(Long cartId) {
            this.cartId = cartId;
        }

        public String getCartName() {
            return cartName;
        }

        public void setCartName(String cartName) {
            this.cartName = cartName;
        }

        public LocalDateTime getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(LocalDateTime creationTime) {
            this.creationTime = creationTime;
        }
    }

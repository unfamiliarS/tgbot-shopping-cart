#!/bin/bash

# set echo
set -x

DB_FILE=test.db
rm -f "$DB_FILE"

sql_query="CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY,
    chat_id INTEGER NOT NULL,
    user_firstname TEXT NOT NULL,
    username TEXT NOT NULL,
    selected_cart_id INTEGER,
    registration_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shopping_carts (
    cart_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cart_name TEXT NOT NULL,
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users_shopping_carts (
    cart_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (cart_id, user_id),
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_url TEXT NOT NULL UNIQUE,
    assigned_cart_id INTEGER NOT NULL,
    product_name TEXT,
    product_price INTEGER,
    adding_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_cart_id) REFERENCES shopping_carts(cart_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS products_shopping_carts (
    cart_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    PRIMARY KEY (cart_id, product_id),
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON UPDATE CASCADE ON DELETE CASCADE
);"

# 'dnf in sqlite -y' for sqlite3 command
sqlite3 "$DB_FILE" << EOF
$sql_query
EOF

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

CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    assigned_cart_id INTEGER NOT NULL,
    category_name TEXT NOT NULL DEFAULT 'Прочее',
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_cart_id) REFERENCES shopping_carts(cart_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_url TEXT NOT NULL,
    assigned_category_id INTEGER NOT NULL,
    product_name TEXT,
    product_price INTEGER,
    product_purchase_status BOOLEAN NOT NULL CHECK (product_purchase_status IN (0, 1)),
    adding_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_category_id) REFERENCES categories(category_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS settings (
    setting_id INTEGER PRIMARY KEY,
    list_already_purchased BOOLEAN NOT NULL CHECK (list_already_purchased IN (0, 1)), 
    notify_about_products BOOLEAN NOT NULL CHECK (notify_about_products IN (0, 1)),
    notify_about_inviting BOOLEAN NOT NULL CHECK (notify_about_inviting IN (0, 1)),
    FOREIGN KEY (setting_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);"

# 'dnf in sqlite -y' for sqlite3 command
sqlite3 "$DB_FILE" << EOF
$sql_query
EOF

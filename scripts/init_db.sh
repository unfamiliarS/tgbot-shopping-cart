#!/bin/bash

DB_FILE=../shopping_cart_bot.db

rm -f "$DB_FILE"

# 'dnf in sqlite -y' for sqlite3 command 
sqlite3 "$DB_FILE" << EOF
CREATE TABLE IF NOT EXISTS shopping_carts (
    cart_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cart_name TEXT NOT NULL,
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_first_name TEXT NOT NULL,
    user_last_name TEXT,
    selected_cart INTEGER DEFAULT -1 NOT NULL,
    registration_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users_shopping_carts (
    cart_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (cart_id, user_id),
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(cart_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);
EOF

echo "Creating $(basename $DB_FILE) is done."
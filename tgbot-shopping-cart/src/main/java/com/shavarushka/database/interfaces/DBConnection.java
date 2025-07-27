package com.shavarushka.database.interfaces;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface DBConnection {

    default Connection connect(String url) throws SQLException {
        return DriverManager.getConnection(url);
    }

    void closeConnection();
}

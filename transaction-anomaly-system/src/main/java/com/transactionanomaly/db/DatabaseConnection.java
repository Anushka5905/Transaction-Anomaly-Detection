package com.transactionanomaly.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a JDBC connection to the local MySQL database.
 * Edit the three constants below to match your own MySQL setup.
 */
public class DatabaseConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/transaction_anomaly_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Anushka@5905"; // <-- change this

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}

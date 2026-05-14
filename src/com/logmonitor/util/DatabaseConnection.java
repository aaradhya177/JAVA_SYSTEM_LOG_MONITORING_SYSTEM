package com.logmonitor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = System.getenv("DATABASE_URL");
            if (url == null) {
                url = "jdbc:postgresql://localhost:5432/logmonitor";
            }
            String user = System.getenv("DB_USER");
            if (user == null) {
                user = "postgres";
            }
            String password = System.getenv("DB_PASSWORD");
            if (password == null) {
                password = "postgres";
            }

            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = System.getenv("DATABASE_URL");
                if (url == null) {
                    url = "jdbc:postgresql://localhost:5432/logmonitor";
                }
                String user = System.getenv("DB_USER");
                if (user == null) {
                    user = "postgres";
                }
                String password = System.getenv("DB_PASSWORD");
                if (password == null) {
                    password = "postgres";
                }
                this.connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

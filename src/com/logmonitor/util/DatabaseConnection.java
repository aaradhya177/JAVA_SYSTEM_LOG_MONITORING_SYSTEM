package com.logmonitor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final String DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5432/logmonitor";
    private final ConnectionConfig config;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.config = resolveConfig();
            validateAndInitialize();
            System.out.println("Connected to database successfully");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver not found", e);
        } catch (SQLException | URISyntaxException e) {
            throw new IllegalStateException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url, config.user, config.password);
    }

    public boolean isDatabaseReachable() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void validateAndInitialize() throws SQLException {
        try (Connection connection = getConnection()) {
            initializeSchema(connection);
        }
    }

    private void initializeSchema(Connection connection) throws SQLException {
        String[] statements = {
                "CREATE TABLE IF NOT EXISTS logs (" +
                        "id SERIAL PRIMARY KEY, " +
                        "timestamp VARCHAR(255) NOT NULL, " +
                        "level VARCHAR(50) NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "source VARCHAR(255) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",
                "CREATE INDEX IF NOT EXISTS idx_logs_level ON logs(level)",
                "CREATE INDEX IF NOT EXISTS idx_logs_source ON logs(source)",
                "CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON logs(timestamp)"
        };

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private ConnectionConfig resolveConfig() throws URISyntaxException {
        String rawUrl = getEnvOrDefault("DATABASE_URL", DEFAULT_JDBC_URL);
        String configuredUser = System.getenv("DB_USER");
        String configuredPassword = System.getenv("DB_PASSWORD");

        if (rawUrl.startsWith("jdbc:postgresql://")) {
            return new ConnectionConfig(
                    rawUrl,
                    getEnvOrDefault("DB_USER", "postgres"),
                    getEnvOrDefault("DB_PASSWORD", "postgres")
            );
        }

        if (rawUrl.startsWith("postgresql://") || rawUrl.startsWith("postgres://")) {
            URI uri = new URI(rawUrl);
            String jdbcUrl = buildJdbcUrl(uri);
            String[] credentials = parseCredentials(uri.getUserInfo());
            String user = configuredUser != null && !configuredUser.isBlank()
                    ? configuredUser
                    : credentials[0];
            String password = configuredPassword != null && !configuredPassword.isBlank()
                    ? configuredPassword
                    : credentials[1];

            if (user == null || user.isBlank()) {
                user = "postgres";
            }
            if (password == null) {
                password = "postgres";
            }

            return new ConnectionConfig(jdbcUrl, user, password);
        }

        return new ConnectionConfig(
                rawUrl,
                getEnvOrDefault("DB_USER", "postgres"),
                getEnvOrDefault("DB_PASSWORD", "postgres")
        );
    }

    private String buildJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());

        if (uri.getPort() != -1) {
            jdbcUrl.append(":").append(uri.getPort());
        }

        jdbcUrl.append(uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/logmonitor" : uri.getRawPath());

        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append("?").append(uri.getRawQuery());
        }

        return jdbcUrl.toString();
    }

    private String[] parseCredentials(String userInfo) {
        String[] credentials = new String[] {null, null};
        if (userInfo == null || userInfo.isBlank()) {
            return credentials;
        }

        String[] parts = userInfo.split(":", 2);
        credentials[0] = parts[0];
        if (parts.length > 1) {
            credentials[1] = parts[1];
        }
        return credentials;
    }

    private String getEnvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static class ConnectionConfig {
        private final String url;
        private final String user;
        private final String password;

        private ConnectionConfig(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }
    }
}

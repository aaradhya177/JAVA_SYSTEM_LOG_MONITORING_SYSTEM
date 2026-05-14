package com.logmonitor.util;

import com.logmonitor.dao.LogDAO;
import com.logmonitor.model.LogEntry;

import java.sql.SQLException;

public final class DemoLogSeeder {
    private static final String[][] DEMO_DATA = {
            {"2024-05-14 10:15:32", "INFO", "Service started on port 8080", "APIGateway"},
            {"2024-05-14 10:20:45", "WARNING", "Memory usage at 78%", "DatabaseService"},
            {"2024-05-14 10:25:12", "ERROR", "NullPointerException in UserController", "AuthService"},
            {"2024-05-14 10:30:22", "INFO", "User login successful", "AuthService"},
            {"2024-05-14 10:35:50", "WARNING", "Slow query detected (2300ms)", "DatabaseService"},
            {"2024-05-14 10:40:15", "ERROR", "Database connection pool exhausted", "DatabaseService"}
    };

    private DemoLogSeeder() {
    }

    public static int seed(LogDAO logDAO) throws SQLException {
        int inserted = 0;
        for (String[] data : DEMO_DATA) {
            LogEntry log = new LogEntry(data[0], data[1], data[2], data[3]);
            logDAO.insertLog(log);
            inserted++;
        }
        return inserted;
    }
}

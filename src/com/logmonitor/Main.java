package com.logmonitor;

import com.logmonitor.dao.LogDAO;
import com.logmonitor.model.LogEntry;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final LogDAO logDAO = new LogDAO();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== Server Log Monitor ===");
        System.out.println("Connected to PostgreSQL Database");
        
        if (args.length > 0 && args[0].equals("--demo")) {
            insertDemoLogs();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        viewAllLogs();
                        break;
                    case "2":
                        filterByLevel(scanner);
                        break;
                    case "3":
                        searchLogs(scanner);
                        break;
                    case "4":
                        filterBySource(scanner);
                        break;
                    case "5":
                        viewStatistics();
                        break;
                    case "6":
                        addLog(scanner);
                        break;
                    case "7":
                        running = false;
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. View All Logs");
        System.out.println("2. Filter by Level (ERROR, WARNING, INFO)");
        System.out.println("3. Search Logs");
        System.out.println("4. Filter by Source");
        System.out.println("5. View Statistics");
        System.out.println("6. Add New Log");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void viewAllLogs() throws SQLException {
        List<LogEntry> logs = logDAO.getAllLogs();
        if (logs.isEmpty()) {
            System.out.println("No logs found.");
        } else {
            System.out.println("\n--- All Logs ---");
            for (LogEntry log : logs) {
                printLog(log);
            }
        }
    }

    private static void filterByLevel(Scanner scanner) throws SQLException {
        System.out.print("Enter log level (ERROR, WARNING, INFO): ");
        String level = scanner.nextLine().trim().toUpperCase();
        List<LogEntry> logs = logDAO.getLogsByLevel(level);
        if (logs.isEmpty()) {
            System.out.println("No logs found with level: " + level);
        } else {
            System.out.println("\n--- Logs with level: " + level + " ---");
            for (LogEntry log : logs) {
                printLog(log);
            }
        }
    }

    private static void searchLogs(Scanner scanner) throws SQLException {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<LogEntry> logs = logDAO.searchLogs(keyword);
        if (logs.isEmpty()) {
            System.out.println("No logs found matching: " + keyword);
        } else {
            System.out.println("\n--- Search Results for: " + keyword + " ---");
            for (LogEntry log : logs) {
                printLog(log);
            }
        }
    }

    private static void filterBySource(Scanner scanner) throws SQLException {
        System.out.print("Enter source: ");
        String source = scanner.nextLine().trim();
        List<LogEntry> logs = logDAO.getLogsBySource(source);
        if (logs.isEmpty()) {
            System.out.println("No logs found from source: " + source);
        } else {
            System.out.println("\n--- Logs from source: " + source + " ---");
            for (LogEntry log : logs) {
                printLog(log);
            }
        }
    }

    private static void viewStatistics() throws SQLException {
        long totalLogs = logDAO.getLogCount();
        long errorCount = logDAO.getErrorCount();
        System.out.println("\n--- Statistics ---");
        System.out.println("Total logs: " + totalLogs);
        System.out.println("Error logs: " + errorCount);
        System.out.println("Success rate: " + ((totalLogs - errorCount) * 100.0 / (totalLogs > 0 ? totalLogs : 1)) + "%");
    }

    private static void addLog(Scanner scanner) throws SQLException {
        System.out.print("Enter message: ");
        String message = scanner.nextLine().trim();
        System.out.print("Enter level (ERROR, WARNING, INFO): ");
        String level = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter source: ");
        String source = scanner.nextLine().trim();
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        LogEntry log = new LogEntry(timestamp, level, message, source);
        logDAO.insertLog(log);
        System.out.println("Log added successfully!");
    }

    private static void insertDemoLogs() {
        String[][] demoData = {
                {"2024-05-14 10:15:32", "INFO", "Service started on port 8080", "APIGateway"},
                {"2024-05-14 10:20:45", "WARNING", "Memory usage at 78%", "DatabaseService"},
                {"2024-05-14 10:25:12", "ERROR", "NullPointerException in UserController", "AuthService"},
                {"2024-05-14 10:30:22", "INFO", "User login successful", "AuthService"},
                {"2024-05-14 10:35:50", "WARNING", "Slow query detected (2300ms)", "DatabaseService"},
                {"2024-05-14 10:40:15", "ERROR", "Database connection pool exhausted", "DatabaseService"}
        };

        try {
            for (String[] data : demoData) {
                LogEntry log = new LogEntry(data[0], data[1], data[2], data[3]);
                logDAO.insertLog(log);
            }
            System.out.println("Demo logs inserted successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to insert demo logs: " + e.getMessage());
        }
    }

    private static void printLog(LogEntry log) {
        System.out.println("[" + log.getTimestamp() + "] " + log.getLevel() + " - " + 
                          log.getSource() + ": " + log.getMessage());
    }
}

package com.logmonitor;

import com.logmonitor.dao.LogDAO;
import com.logmonitor.model.LogEntry;
import com.logmonitor.server.HttpApiServer;
import com.logmonitor.service.LogService;
import com.logmonitor.service.LogStatistics;
import com.logmonitor.ui.LogMonitorFrame;
import com.logmonitor.util.DatabaseConnection;

import java.io.IOException;
import java.awt.GraphicsEnvironment;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final LogDAO logDAO = new LogDAO();
    private static final LogService logService = new LogService(logDAO);

    public static void main(String[] args) {
        try {
            DatabaseConnection.getInstance();
            System.out.println("=== Server Log Monitor ===");

            if (containsArg(args, "--demo")) {
                insertDemoLogs();
                return;
            }

            if (containsArg(args, "--cli")) {
                runCli();
                return;
            }

            if (shouldRunHttpServer(args)) {
                startHttpServer();
                return;
            }

            if (!GraphicsEnvironment.isHeadless()) {
                startDesktopApp();
                return;
            }

            runCli();
        } catch (IllegalStateException e) {
            System.err.println("Application startup failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runCli() {
        System.out.println("Connected to PostgreSQL database");
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

    private static boolean containsArg(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldRunHttpServer(String[] args) {
        return containsArg(args, "--server") || hasEnvValue("PORT");
    }

    private static boolean hasEnvValue(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }

    private static void startHttpServer() {
        int port = resolvePort();
        try {
            HttpApiServer server = new HttpApiServer(port);
            server.start();
            System.out.println("HTTP server running on port " + port);
            System.out.println("Health check: /health");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start HTTP server on port " + port, e);
        }
    }

    private static void startDesktopApp() {
        LogMonitorFrame.launch(logService);
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        if (portValue == null || portValue.isBlank()) {
            return 8080;
        }

        try {
            return Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid PORT value: " + portValue, e);
        }
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
        List<LogEntry> logs = logService.getAllLogs();
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
        List<LogEntry> logs = logService.getLogs(level, null, null);
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
        List<LogEntry> logs = logService.getLogs(null, null, keyword);
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
        List<LogEntry> logs = logService.getLogs(null, source, null);
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
        LogStatistics statistics = logService.getStatistics();
        System.out.println("\n--- Statistics ---");
        System.out.println("Total logs: " + statistics.getTotalLogs());
        System.out.println("Error logs: " + statistics.getErrorLogs());
        System.out.println("Success rate: " + statistics.getSuccessRate() + "%");
    }

    private static void addLog(Scanner scanner) throws SQLException {
        System.out.print("Enter message: ");
        String message = scanner.nextLine().trim();
        System.out.print("Enter level (ERROR, WARNING, INFO): ");
        String level = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter source: ");
        String source = scanner.nextLine().trim();

        logService.addLog(level, message, source);
        System.out.println("Log added successfully!");
    }

    private static void insertDemoLogs() {
        try {
            int inserted = logService.seedDemoLogs();
            System.out.println("Demo logs inserted successfully! Inserted " + inserted + " records.");
        } catch (SQLException e) {
            System.err.println("Failed to insert demo logs: " + e.getMessage());
        }
    }

    private static void printLog(LogEntry log) {
        System.out.println("[" + log.getTimestamp() + "] " + log.getLevel() + " - " + 
                          log.getSource() + ": " + log.getMessage());
    }
}

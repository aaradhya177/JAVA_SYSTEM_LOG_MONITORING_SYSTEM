package com.logmonitor.server;

import com.logmonitor.dao.LogDAO;
import com.logmonitor.model.LogEntry;
import com.logmonitor.util.DatabaseConnection;
import com.logmonitor.util.DemoLogSeeder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpApiServer {
    private final HttpServer server;
    private final LogDAO logDAO;
    private final DatabaseConnection databaseConnection;

    public HttpApiServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(Executors.newFixedThreadPool(8));
        this.logDAO = new LogDAO();
        this.databaseConnection = DatabaseConnection.getInstance();
        configureRoutes();
    }

    public void start() {
        server.start();
    }

    private void configureRoutes() {
        server.createContext("/", this::handleRoot);
        server.createContext("/health", this::handleHealth);
        server.createContext("/logs", this::handleLogs);
        server.createContext("/stats", this::handleStats);
        server.createContext("/demo", this::handleDemo);
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        if (!isMethod(exchange, "GET")) {
            sendMethodNotAllowed(exchange, "GET");
            return;
        }

        String response = "{\"service\":\"Server Log Monitor\",\"status\":\"running\",\"endpoints\":[\"/health\",\"/logs\",\"/stats\",\"/demo\"]}";
        sendJson(exchange, 200, response);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!isMethod(exchange, "GET")) {
            sendMethodNotAllowed(exchange, "GET");
            return;
        }

        boolean reachable = databaseConnection.isDatabaseReachable();
        int statusCode = reachable ? 200 : 503;
        String response = "{\"status\":\"" + (reachable ? "ok" : "error") + "\",\"database\":\"" +
                (reachable ? "connected" : "unreachable") + "\"}";
        sendJson(exchange, statusCode, response);
    }

    private void handleLogs(HttpExchange exchange) throws IOException {
        if (!isMethod(exchange, "GET")) {
            sendMethodNotAllowed(exchange, "GET");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());

        try {
            List<LogEntry> logs;
            if (params.containsKey("search")) {
                logs = logDAO.searchLogs(params.get("search"));
            } else if (params.containsKey("level")) {
                logs = logDAO.getLogsByLevel(params.get("level").toUpperCase());
            } else if (params.containsKey("source")) {
                logs = logDAO.getLogsBySource(params.get("source"));
            } else {
                logs = logDAO.getAllLogs();
            }

            StringBuilder response = new StringBuilder("[");
            for (int i = 0; i < logs.size(); i++) {
                if (i > 0) {
                    response.append(",");
                }
                response.append(toJson(logs.get(i)));
            }
            response.append("]");
            sendJson(exchange, 200, response.toString());
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleStats(HttpExchange exchange) throws IOException {
        if (!isMethod(exchange, "GET")) {
            sendMethodNotAllowed(exchange, "GET");
            return;
        }

        try {
            long totalLogs = logDAO.getLogCount();
            long errorLogs = logDAO.getErrorCount();
            double successRate = (totalLogs - errorLogs) * 100.0 / (totalLogs > 0 ? totalLogs : 1);
            String response = "{\"totalLogs\":" + totalLogs +
                    ",\"errorLogs\":" + errorLogs +
                    ",\"successRate\":" + String.format(java.util.Locale.US, "%.2f", successRate) + "}";
            sendJson(exchange, 200, response);
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDemo(HttpExchange exchange) throws IOException {
        if (!isMethod(exchange, "POST")) {
            sendMethodNotAllowed(exchange, "POST");
            return;
        }

        try {
            int inserted = DemoLogSeeder.seed(logDAO);
            sendJson(exchange, 201, "{\"inserted\":" + inserted + "}");
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private boolean isMethod(HttpExchange exchange, String expected) {
        return expected.equalsIgnoreCase(exchange.getRequestMethod());
    }

    private void sendMethodNotAllowed(HttpExchange exchange, String allowedMethod) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethod);
        sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
    }

    private void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String toJson(LogEntry log) {
        return "{\"id\":" + log.getId() +
                ",\"timestamp\":\"" + escapeJson(log.getTimestamp()) + "\"" +
                ",\"level\":\"" + escapeJson(log.getLevel()) + "\"" +
                ",\"message\":\"" + escapeJson(log.getMessage()) + "\"" +
                ",\"source\":\"" + escapeJson(log.getSource()) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

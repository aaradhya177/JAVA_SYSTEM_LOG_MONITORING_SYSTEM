package com.logmonitor.service;

import com.logmonitor.dao.LogDAO;
import com.logmonitor.model.LogEntry;
import com.logmonitor.util.DemoLogSeeder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LogDAO logDAO;

    public LogService() {
        this(new LogDAO());
    }

    public LogService(LogDAO logDAO) {
        this.logDAO = logDAO;
    }

    public List<LogEntry> getAllLogs() throws SQLException {
        return logDAO.getAllLogs();
    }

    public List<LogEntry> getLogs(String level, String source, String search) throws SQLException {
        String normalizedLevel = normalize(level);
        String normalizedSource = normalize(source);
        String normalizedSearch = normalize(search);

        if (normalizedLevel == null && normalizedSource == null && normalizedSearch == null) {
            return logDAO.getAllLogs();
        }

        List<LogEntry> candidateLogs;
        if (normalizedSearch != null) {
            candidateLogs = logDAO.searchLogs(normalizedSearch);
        } else if (normalizedLevel != null) {
            candidateLogs = logDAO.getLogsByLevel(normalizedLevel);
        } else if (normalizedSource != null) {
            candidateLogs = logDAO.getLogsBySource(normalizedSource);
        } else {
            candidateLogs = logDAO.getAllLogs();
        }

        List<LogEntry> filteredLogs = new ArrayList<>();
        for (LogEntry log : candidateLogs) {
            boolean matchesLevel = normalizedLevel == null || normalizedLevel.equalsIgnoreCase(log.getLevel());
            boolean matchesSource = normalizedSource == null || normalizedSource.equalsIgnoreCase(log.getSource());
            boolean matchesSearch = normalizedSearch == null
                    || containsIgnoreCase(log.getMessage(), normalizedSearch)
                    || containsIgnoreCase(log.getSource(), normalizedSearch);

            if (matchesLevel && matchesSource && matchesSearch) {
                filteredLogs.add(log);
            }
        }
        return filteredLogs;
    }

    public LogStatistics getStatistics() throws SQLException {
        return new LogStatistics(logDAO.getLogCount(), logDAO.getErrorCount());
    }

    public List<String> getDistinctSources() throws SQLException {
        return logDAO.getDistinctSources();
    }

    public void addLog(String level, String message, String source) throws SQLException {
        LogEntry log = new LogEntry(
                LocalDateTime.now().format(FORMATTER),
                level.toUpperCase(),
                message.trim(),
                source.trim()
        );
        logDAO.insertLog(log);
    }

    public void deleteLog(int id) throws SQLException {
        logDAO.deleteLog(id);
    }

    public int seedDemoLogs() throws SQLException {
        return DemoLogSeeder.seed(logDAO);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() || "ALL".equalsIgnoreCase(trimmed) ? null : trimmed;
    }

    private boolean containsIgnoreCase(String value, String token) {
        return value != null && value.toLowerCase().contains(token.toLowerCase());
    }
}

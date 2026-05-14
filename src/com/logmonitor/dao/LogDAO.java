package com.logmonitor.dao;

import com.logmonitor.model.LogEntry;
import com.logmonitor.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    public void insertLog(LogEntry log) throws SQLException {
        String sql = "INSERT INTO logs (timestamp, level, message, source) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, log.getTimestamp());
            stmt.setString(2, log.getLevel());
            stmt.setString(3, log.getMessage());
            stmt.setString(4, log.getSource());
            stmt.executeUpdate();
        }
    }

    public List<LogEntry> getAllLogs() throws SQLException {
        String sql = "SELECT id, timestamp, level, message, source FROM logs ORDER BY timestamp DESC";
        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new LogEntry(
                        rs.getInt("id"),
                        rs.getString("timestamp"),
                        rs.getString("level"),
                        rs.getString("message"),
                        rs.getString("source")
                ));
            }
        }
        return logs;
    }

    public List<LogEntry> getLogsByLevel(String level) throws SQLException {
        String sql = "SELECT id, timestamp, level, message, source FROM logs WHERE level = ? ORDER BY timestamp DESC";
        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, level);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new LogEntry(
                            rs.getInt("id"),
                            rs.getString("timestamp"),
                            rs.getString("level"),
                            rs.getString("message"),
                            rs.getString("source")
                    ));
                }
            }
        }
        return logs;
    }

    public List<LogEntry> searchLogs(String keyword) throws SQLException {
        String sql = "SELECT id, timestamp, level, message, source FROM logs WHERE message ILIKE ? OR source ILIKE ? ORDER BY timestamp DESC";
        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new LogEntry(
                            rs.getInt("id"),
                            rs.getString("timestamp"),
                            rs.getString("level"),
                            rs.getString("message"),
                            rs.getString("source")
                    ));
                }
            }
        }
        return logs;
    }

    public List<LogEntry> getLogsBySource(String source) throws SQLException {
        String sql = "SELECT id, timestamp, level, message, source FROM logs WHERE source = ? ORDER BY timestamp DESC";
        List<LogEntry> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, source);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new LogEntry(
                            rs.getInt("id"),
                            rs.getString("timestamp"),
                            rs.getString("level"),
                            rs.getString("message"),
                            rs.getString("source")
                    ));
                }
            }
        }
        return logs;
    }

    public List<String> getDistinctSources() throws SQLException {
        String sql = "SELECT DISTINCT source FROM logs ORDER BY source ASC";
        List<String> sources = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sources.add(rs.getString("source"));
            }
        }
        return sources;
    }

    public void deleteLog(int id) throws SQLException {
        String sql = "DELETE FROM logs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public long getLogCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM logs";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }

    public long getErrorCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM logs WHERE level = 'ERROR'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }
}

package com.logmonitor.model;

public class LogEntry {
    private int id;
    private String timestamp;
    private String level;
    private String message;
    private String source;

    public LogEntry(String timestamp, String level, String message, String source) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.source = source;
    }

    public LogEntry(int id, String timestamp, String level, String message, String source) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "id=" + id +
                ", timestamp='" + timestamp + '\'' +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}

package com.logmonitor.service;

public class LogStatistics {
    private final long totalLogs;
    private final long errorLogs;
    private final double successRate;

    public LogStatistics(long totalLogs, long errorLogs) {
        this.totalLogs = totalLogs;
        this.errorLogs = errorLogs;
        this.successRate = (totalLogs - errorLogs) * 100.0 / (totalLogs > 0 ? totalLogs : 1);
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public long getErrorLogs() {
        return errorLogs;
    }

    public double getSuccessRate() {
        return successRate;
    }
}

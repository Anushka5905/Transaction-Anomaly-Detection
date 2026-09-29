package com.anomaly.transactionanomalybackend.dto;

public class DashboardStats {

    private long totalTransactions;
    private long totalAnomalies;
    private long highRiskCount;
    private long suspiciousCount;
    private double averageRiskScore;

    public DashboardStats() {
    }

    public DashboardStats(
            long totalTransactions,
            long totalAnomalies,
            long highRiskCount,
            long suspiciousCount,
            double averageRiskScore) {

        this.totalTransactions = totalTransactions;
        this.totalAnomalies = totalAnomalies;
        this.highRiskCount = highRiskCount;
        this.suspiciousCount = suspiciousCount;
        this.averageRiskScore = averageRiskScore;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getTotalAnomalies() {
        return totalAnomalies;
    }

    public void setTotalAnomalies(long totalAnomalies) {
        this.totalAnomalies = totalAnomalies;
    }

    public long getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(long highRiskCount) {
        this.highRiskCount = highRiskCount;
    }

    public long getSuspiciousCount() {
        return suspiciousCount;
    }

    public void setSuspiciousCount(long suspiciousCount) {
        this.suspiciousCount = suspiciousCount;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(double averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }
}
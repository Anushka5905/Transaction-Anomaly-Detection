package com.transactionanomaly.model;

import java.time.LocalDateTime;

/**
 * A simple "plain data" class that represents one row in the transactions table.
 * No logic here on purpose - just fields + getters/setters, so it's easy to read.
 */
public class Transaction {

    private int id;
    private String userId;
    private double amount;
    private LocalDateTime transactionTime;
    private String type;       // e.g. PURCHASE, TRANSFER, WITHDRAWAL
    private String location;

    private double anomalyScore; // 0.0 (normal) -> 1.0 (very suspicious)
    private boolean anomaly;     // true if flagged as suspicious

    public Transaction() {
    }

    public Transaction(String userId, double amount, LocalDateTime transactionTime,
                        String type, String location) {
        this.userId = userId;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.type = type;
        this.location = location;
    }

    // ----- Getters and setters -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public boolean isAnomaly() {
        return anomaly;
    }

    public void setAnomaly(boolean anomaly) {
        this.anomaly = anomaly;
    }
}

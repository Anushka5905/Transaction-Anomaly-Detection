package com.anomaly.transactionanomalybackend.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsStats {

    private double anomalyRate;

    private long normalTransactions;

    private long highRiskTransactions;

    private long suspiciousTransactions;

    private long openReviews;

    private long investigatingReviews;

    private long confirmedReviews;

    private long falsePositiveReviews;

    private List<Map<String, Object>> dailyTransactions;

    private List<Map<String, Object>> dailyRisk;

    public AnalyticsStats() {
    }

    public double getAnomalyRate() {
        return anomalyRate;
    }

    public void setAnomalyRate(double anomalyRate) {
        this.anomalyRate = anomalyRate;
    }

    public long getNormalTransactions() {
        return normalTransactions;
    }

    public void setNormalTransactions(long normalTransactions) {
        this.normalTransactions = normalTransactions;
    }

    public long getHighRiskTransactions() {
        return highRiskTransactions;
    }

    public void setHighRiskTransactions(long highRiskTransactions) {
        this.highRiskTransactions = highRiskTransactions;
    }

    public long getSuspiciousTransactions() {
        return suspiciousTransactions;
    }

    public void setSuspiciousTransactions(long suspiciousTransactions) {
        this.suspiciousTransactions = suspiciousTransactions;
    }

    public long getOpenReviews() {
        return openReviews;
    }

    public void setOpenReviews(long openReviews) {
        this.openReviews = openReviews;
    }

    public long getInvestigatingReviews() {
        return investigatingReviews;
    }

    public void setInvestigatingReviews(long investigatingReviews) {
        this.investigatingReviews = investigatingReviews;
    }

    public long getConfirmedReviews() {
        return confirmedReviews;
    }

    public void setConfirmedReviews(long confirmedReviews) {
        this.confirmedReviews = confirmedReviews;
    }

    public long getFalsePositiveReviews() {
        return falsePositiveReviews;
    }

    public void setFalsePositiveReviews(long falsePositiveReviews) {
        this.falsePositiveReviews = falsePositiveReviews;
    }

    public List<Map<String, Object>> getDailyTransactions() {
        return dailyTransactions;
    }

    public void setDailyTransactions(List<Map<String, Object>> dailyTransactions) {
        this.dailyTransactions = dailyTransactions;
    }

    public List<Map<String, Object>> getDailyRisk() {
        return dailyRisk;
    }

    public void setDailyRisk(List<Map<String, Object>> dailyRisk) {
        this.dailyRisk = dailyRisk;
    }
}
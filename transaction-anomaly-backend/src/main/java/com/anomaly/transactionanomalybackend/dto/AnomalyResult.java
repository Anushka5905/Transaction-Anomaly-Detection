package com.anomaly.transactionanomalybackend.dto;

import com.anomaly.transactionanomalybackend.model.Transaction;

import java.util.List;

public class AnomalyResult {

    private Transaction transaction;

    private double mlScore;

    private double ruleScore;

    private double riskScore;

    private String status;

    private List<String> reasons;

    public AnomalyResult() {
    }

    public AnomalyResult(
            Transaction transaction,
            double mlScore,
            double ruleScore,
            double riskScore,
            String status,
            List<String> reasons) {

        this.transaction = transaction;
        this.mlScore = mlScore;
        this.ruleScore = ruleScore;
        this.riskScore = riskScore;
        this.status = status;
        this.reasons = reasons;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public double getMlScore() {
        return mlScore;
    }

    public void setMlScore(double mlScore) {
        this.mlScore = mlScore;
    }

    public double getRuleScore() {
        return ruleScore;
    }

    public void setRuleScore(double ruleScore) {
        this.ruleScore = ruleScore;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
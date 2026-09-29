package com.anomaly.transactionanomalybackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anomalies")
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;

    private Long userId;

    private Double mlScore;

    private Double ruleScore;

    private Double riskScore;

    // Detection status: HIGH_RISK / SUSPICIOUS
    private String status;

    @Column(length = 1000)
    private String reasons;

    private LocalDateTime detectedAt;

    // ---------------- REVIEW FIELDS ----------------

    // OPEN / INVESTIGATING / CONFIRMED / FALSE_POSITIVE
    @Column(nullable = false)
    private String reviewStatus = "OPEN";

    // Notes added by analyst/admin during investigation
    @Column(length = 2000)
    private String reviewNotes;

    // Email of the person who reviewed the anomaly
    private String reviewedBy;

    // Date and time when the anomaly was reviewed
    private LocalDateTime reviewedAt;

    // ------------------------------------------------

    public Anomaly() {
    }

    public Anomaly(
            Long transactionId,
            Long userId,
            Double mlScore,
            Double ruleScore,
            Double riskScore,
            String status,
            String reasons,
            LocalDateTime detectedAt) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.mlScore = mlScore;
        this.ruleScore = ruleScore;
        this.riskScore = riskScore;
        this.status = status;
        this.reasons = reasons;
        this.detectedAt = detectedAt;

        // Every newly detected anomaly starts as OPEN
        this.reviewStatus = "OPEN";
    }

    // ---------------- GETTERS & SETTERS ----------------

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getMlScore() {
        return mlScore;
    }

    public void setMlScore(Double mlScore) {
        this.mlScore = mlScore;
    }

    public Double getRuleScore() {
        return ruleScore;
    }

    public void setRuleScore(Double ruleScore) {
        this.ruleScore = ruleScore;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    // ---------------- REVIEW GETTERS & SETTERS ----------------

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
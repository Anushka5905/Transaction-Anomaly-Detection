package com.anomaly.transactionanomalybackend.service;

import com.anomaly.transactionanomalybackend.dto.AnalyticsStats;
import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.model.Transaction;
import com.anomaly.transactionanomalybackend.repository.AnomalyRepository;
import com.anomaly.transactionanomalybackend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;

    public AnalyticsService(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository) {

        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
    }

    public AnalyticsStats getAnalytics() {

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<Anomaly> anomalies =
                anomalyRepository.findAll();

        AnalyticsStats stats = new AnalyticsStats();

        long totalTransactions = transactions.size();
        long totalAnomalies = anomalies.size();

        // =====================================================
        // ANOMALY RATE
        // =====================================================

        double anomalyRate = 0.0;

        if (totalTransactions > 0) {
            anomalyRate =
                    ((double) totalAnomalies / totalTransactions) * 100.0;
        }

        stats.setAnomalyRate(
                Math.round(anomalyRate * 100.0) / 100.0
        );

        // =====================================================
        // RISK CLASSIFICATION
        // =====================================================

        long highRisk = 0;
        long suspicious = 0;

        for (Anomaly anomaly : anomalies) {

            String status = anomaly.getStatus();

            if ("HIGH_RISK".equalsIgnoreCase(status)) {
                highRisk++;
            } else if ("SUSPICIOUS".equalsIgnoreCase(status)) {
                suspicious++;
            }
        }

        long normal =
                Math.max(
                        0,
                        totalTransactions - totalAnomalies
                );

        stats.setHighRiskTransactions(highRisk);
        stats.setSuspiciousTransactions(suspicious);
        stats.setNormalTransactions(normal);

        // =====================================================
        // REVIEW STATUS
        // =====================================================

        long open = 0;
        long investigating = 0;
        long confirmed = 0;
        long falsePositive = 0;

        for (Anomaly anomaly : anomalies) {

            String reviewStatus = anomaly.getReviewStatus();

            if (reviewStatus == null ||
                    reviewStatus.isBlank() ||
                    "OPEN".equalsIgnoreCase(reviewStatus)) {

                open++;

            } else if ("INVESTIGATING".equalsIgnoreCase(reviewStatus)) {

                investigating++;

            } else if ("CONFIRMED".equalsIgnoreCase(reviewStatus)) {

                confirmed++;

            } else if ("FALSE_POSITIVE".equalsIgnoreCase(reviewStatus)) {

                falsePositive++;
            }
        }

        stats.setOpenReviews(open);
        stats.setInvestigatingReviews(investigating);
        stats.setConfirmedReviews(confirmed);
        stats.setFalsePositiveReviews(falsePositive);

        // =====================================================
        // DAILY TRANSACTION ACTIVITY
        // =====================================================

        Map<LocalDate, Long> transactionMap =
                new LinkedHashMap<>();

        for (Transaction transaction : transactions) {

            if (transaction.getTransactionTime() == null) {
                continue;
            }

            LocalDate date =
                    transaction.getTransactionTime().toLocalDate();

            transactionMap.put(
                    date,
                    transactionMap.getOrDefault(date, 0L) + 1
            );
        }

        List<Map<String, Object>> dailyTransactions =
                new ArrayList<>();

        transactionMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "date",
                            entry.getKey().toString()
                    );

                    item.put(
                            "count",
                            entry.getValue()
                    );

                    dailyTransactions.add(item);
                });

        stats.setDailyTransactions(dailyTransactions);

        // =====================================================
        // DAILY RISK TREND
        // =====================================================

        Map<LocalDate, Double> riskTotals =
                new LinkedHashMap<>();

        Map<LocalDate, Long> riskCounts =
                new LinkedHashMap<>();

        for (Anomaly anomaly : anomalies) {

            if (anomaly.getDetectedAt() == null) {
                continue;
            }

            LocalDate date =
                    anomaly.getDetectedAt().toLocalDate();

            double risk =
                    anomaly.getRiskScore();

            riskTotals.put(
                    date,
                    riskTotals.getOrDefault(date, 0.0) + risk
            );

            riskCounts.put(
                    date,
                    riskCounts.getOrDefault(date, 0L) + 1
            );
        }

        List<Map<String, Object>> dailyRisk =
                new ArrayList<>();

        riskTotals.keySet()
                .stream()
                .sorted(Comparator.naturalOrder())
                .forEach(date -> {

                    long count =
                            riskCounts.getOrDefault(date, 1L);

                    double totalRisk =
                            riskTotals.getOrDefault(date, 0.0);

                    double averageRisk =
                            totalRisk / count;

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "date",
                            date.toString()
                    );

                    item.put(
                            "averageRisk",
                            Math.round(averageRisk * 100.0) / 100.0
                    );

                    dailyRisk.add(item);
                });

        stats.setDailyRisk(dailyRisk);

        return stats;
    }
}
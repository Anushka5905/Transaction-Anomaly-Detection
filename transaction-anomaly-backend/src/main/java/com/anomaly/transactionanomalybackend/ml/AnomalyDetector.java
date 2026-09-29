package com.anomaly.transactionanomalybackend.ml;

import com.anomaly.transactionanomalybackend.dto.AnomalyResult;
import com.anomaly.transactionanomalybackend.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnomalyDetector {

    private final IsolationForestDetector isolationForestDetector;

    public AnomalyDetector(
            IsolationForestDetector isolationForestDetector) {

        this.isolationForestDetector =
                isolationForestDetector;
    }

    public List<AnomalyResult> analyzeTransactions(
            List<Transaction> transactions) {

        List<AnomalyResult> results =
                new ArrayList<>();

        if (transactions == null ||
                transactions.size() < 4) {

            return results;
        }

        // -----------------------------------------
        // MACHINE LEARNING SCORES
        // -----------------------------------------

        List<Double> mlScores =
                isolationForestDetector
                        .calculateAnomalyScores(transactions);

        if (mlScores.size() != transactions.size()) {
            return results;
        }

        // -----------------------------------------
        // OVERALL AMOUNT STATISTICS
        // -----------------------------------------

        List<Double> amounts =
                transactions.stream()
                        .map(Transaction::getAmount)
                        .filter(amount -> amount != null)
                        .sorted()
                        .toList();

        if (amounts.size() < 4) {
            return results;
        }

        double q1 =
                calculateMedian(
                        amounts.subList(
                                0,
                                amounts.size() / 2
                        )
                );

        double q3 =
                calculateMedian(
                        amounts.subList(
                                (amounts.size() + 1) / 2,
                                amounts.size()
                        )
                );

        double iqr = q3 - q1;

        double upperBound =
                q3 + (1.5 * iqr);

        // -----------------------------------------
        // ANALYZE EACH TRANSACTION
        // -----------------------------------------

        for (int i = 0;
             i < transactions.size();
             i++) {

            Transaction transaction =
                    transactions.get(i);

            if (transaction.getAmount() == null) {
                continue;
            }

            double amount =
                    transaction.getAmount();

            double mlScore =
                    mlScores.get(i);

            double mlRiskScore =
                    calculateMlRiskScore(mlScore);

            double ruleRiskScore = 0;

            List<String> reasons =
                    new ArrayList<>();

            // -----------------------------------------
            // RULE 1: MACHINE LEARNING
            // -----------------------------------------

            if (mlScore >= 0.60) {

                reasons.add(
                        "Isolation Forest detected unusual transaction behavior"
                );
            }

            // -----------------------------------------
            // USER TRANSACTION HISTORY
            // -----------------------------------------

            Long currentUserId =
                    transaction.getUserId();

            List<Transaction> userTransactions =
                    transactions.stream()
                            .filter(t ->
                                    currentUserId != null &&
                                    currentUserId.equals(
                                            t.getUserId()
                                    )
                            )
                            .filter(t ->
                                    t.getId() == null ||
                                    transaction.getId() == null ||
                                    !t.getId().equals(
                                            transaction.getId()
                                    )
                            )
                            .toList();

            // -----------------------------------------
            // RULE 2: STATISTICAL AMOUNT ANOMALY
            // -----------------------------------------

            if (amount > upperBound) {

                ruleRiskScore += 30;

                reasons.add(
                        "Transaction amount is statistically unusual"
                );
            }

            // -----------------------------------------
            // RULE 3: USER-SPECIFIC AMOUNT ANOMALY
            // -----------------------------------------

            if (!userTransactions.isEmpty()) {

                double userAverage =
                        userTransactions.stream()
                                .map(Transaction::getAmount)
                                .filter(a -> a != null)
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0);

                if (userAverage > 0 &&
                        amount > userAverage * 3) {

                    ruleRiskScore += 35;

                    reasons.add(
                            "Transaction amount is significantly higher than user's average"
                    );
                }
            }

            // -----------------------------------------
            // RULE 4: UNUSUAL TIME
            // -----------------------------------------

            if (transaction.getTransactionTime() != null) {

                int hour =
                        transaction.getTransactionTime()
                                .getHour();

                if (hour < 6 || hour >= 23) {

                    ruleRiskScore += 15;

                    reasons.add(
                            "Transaction occurred at an unusual hour"
                    );
                }
            }

            // -----------------------------------------
            // RULE 5: UNUSUAL LOCATION
            // -----------------------------------------

            if (!userTransactions.isEmpty() &&
                    transaction.getLocation() != null) {

                Map<String, Long> locationCounts =
                        userTransactions.stream()
                                .filter(t ->
                                        t.getLocation() != null
                                )
                                .collect(
                                        Collectors.groupingBy(
                                                Transaction::getLocation,
                                                Collectors.counting()
                                        )
                                );

                String usualLocation =
                        locationCounts.entrySet()
                                .stream()
                                .max(
                                        Map.Entry.comparingByValue()
                                )
                                .map(
                                        Map.Entry::getKey
                                )
                                .orElse(null);

                if (usualLocation != null &&
                        !usualLocation.equalsIgnoreCase(
                                transaction.getLocation()
                        )) {

                    ruleRiskScore += 15;

                    reasons.add(
                            "Transaction location differs from user's usual location"
                    );
                }
            }

            // -----------------------------------------
            // RULE 6: RAPID TRANSACTIONS
            // -----------------------------------------

            if (transaction.getTransactionTime() != null) {

                long transactionsWithinOneHour =
                        userTransactions.stream()
                                .filter(t ->
                                        t.getTransactionTime() != null
                                )
                                .filter(t -> {

                                    long minutes =
                                            Math.abs(
                                                    Duration.between(
                                                            transaction
                                                                    .getTransactionTime(),
                                                            t
                                                                    .getTransactionTime()
                                                    ).toMinutes()
                                            );

                                    return minutes <= 60;
                                })
                                .count();

                if (transactionsWithinOneHour >= 3) {

                    ruleRiskScore += 15;

                    reasons.add(
                            "Multiple transactions detected within a short time period"
                    );
                }
            }

            // -----------------------------------------
            // LIMIT RULE SCORE
            // -----------------------------------------

            ruleRiskScore =
                    Math.min(
                            ruleRiskScore,
                            70
                    );

            // -----------------------------------------
            // FINAL RISK SCORE
            //
            // 40% ML
            // 60% rule-based analysis
            // -----------------------------------------

            double finalRiskScore =
                    (mlRiskScore * 0.40)
                            + (ruleRiskScore * 0.60);

            finalRiskScore =
                    Math.min(
                            Math.max(
                                    finalRiskScore,
                                    0
                            ),
                            100
                    );

            // -----------------------------------------
            // RISK LEVEL
            // -----------------------------------------

            String status;

            if (finalRiskScore >= 70) {

                status = "HIGH_RISK";

            } else if (finalRiskScore >= 30) {

                status = "SUSPICIOUS";

            } else {

                status = "NORMAL";
            }

            // -----------------------------------------
            // RETURN ONLY SUSPICIOUS TRANSACTIONS
            // -----------------------------------------

            if (!status.equals("NORMAL")) {

                results.add(
                        new AnomalyResult(
                                transaction,
                                mlRiskScore,
                                ruleRiskScore,
                                Math.round(
                                        finalRiskScore * 100.0
                                ) / 100.0,
                                status,
                                reasons
                        )
                );
            }
        }

        return results;
    }

    // -----------------------------------------
    // CONVERT ML SCORE TO 0-100
    // -----------------------------------------

    private double calculateMlRiskScore(
            double mlScore) {

        double normalized =
                Math.max(
                        0,
                        Math.min(
                                mlScore,
                                1
                        )
                );

        return normalized * 100;
    }

    // -----------------------------------------
    // MEDIAN CALCULATION
    // -----------------------------------------

    private double calculateMedian(
            List<Double> values) {

        if (values.isEmpty()) {
            return 0;
        }

        List<Double> sortedValues =
                new ArrayList<>(values);

        Collections.sort(sortedValues);

        int size =
                sortedValues.size();

        if (size % 2 == 0) {

            return (
                    sortedValues.get(size / 2 - 1)
                            + sortedValues.get(size / 2)
            ) / 2.0;
        }

        return sortedValues.get(size / 2);
    }
}
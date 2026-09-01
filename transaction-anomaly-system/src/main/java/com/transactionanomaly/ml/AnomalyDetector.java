package com.transactionanomaly.ml;

import com.transactionanomaly.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects unusual transactions using the Interquartile Range (IQR) method.
 *
 * The detector mainly checks transaction amount.
 *
 * Q1 = 25th percentile
 * Q3 = 75th percentile
 * IQR = Q3 - Q1
 *
 * Normal upper limit  = Q3 + 1.5 * IQR
 * Extreme upper limit = Q3 + 3.0 * IQR
 *
 * If amount is above the extreme limit -> anomaly score 1.0
 * If amount is above the normal limit  -> anomaly score 0.6
 * Otherwise                         -> anomaly score 0.0
 */
public class AnomalyDetector {

    public List<Transaction> detectAnomalies(List<Transaction> transactions) {

        if (transactions == null || transactions.isEmpty()) {
            return transactions;
        }

        // Get all transaction amounts
        List<Double> amounts = new ArrayList<>();

        for (Transaction t : transactions) {
            amounts.add(t.getAmount());
        }

        // Sort amounts from smallest to largest
        Collections.sort(amounts);

        // Calculate Q1 and Q3
        double q1 = calculatePercentile(amounts, 25);
        double q3 = calculatePercentile(amounts, 75);

        // Calculate IQR
        double iqr = q3 - q1;

        // Define limits
        double normalLimit = q3 + (1.5 * iqr);
        double extremeLimit = q3 + (3.0 * iqr);

        System.out.println("=================================");
        System.out.println("ANOMALY DETECTION");
        System.out.println("Q1: " + q1);
        System.out.println("Q3: " + q3);
        System.out.println("IQR: " + iqr);
        System.out.println("Normal Limit: " + normalLimit);
        System.out.println("Extreme Limit: " + extremeLimit);
        System.out.println("=================================");

        // Check every transaction
        for (Transaction t : transactions) {

            double amount = t.getAmount();

            double score;
            boolean anomaly;

            if (amount > extremeLimit) {

                // Extremely unusual transaction
                score = 1.0;
                anomaly = true;

            } else if (amount > normalLimit) {

                // Unusual transaction
                score = 0.6;
                anomaly = true;

            } else {

                // Normal transaction
                score = 0.0;
                anomaly = false;
            }

            t.setAnomalyScore(score);
            t.setAnomaly(anomaly);
        }

        return transactions;
    }

    /**
     * Calculates a percentile from a sorted list.
     */
    private double calculatePercentile(List<Double> values, double percentile) {

        if (values.size() == 1) {
            return values.get(0);
        }

        double index = (percentile / 100.0) * (values.size() - 1);

        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);

        if (lowerIndex == upperIndex) {
            return values.get(lowerIndex);
        }

        double weight = index - lowerIndex;

        return values.get(lowerIndex)
                + weight * (values.get(upperIndex) - values.get(lowerIndex));
    }
}
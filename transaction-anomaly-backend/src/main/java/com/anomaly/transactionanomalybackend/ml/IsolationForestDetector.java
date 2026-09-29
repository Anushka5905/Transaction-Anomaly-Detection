package com.anomaly.transactionanomalybackend.ml;

import com.anomaly.transactionanomalybackend.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class IsolationForestDetector {

    private static final int NUMBER_OF_TREES = 50;
    private static final int SAMPLE_SIZE = 256;

    private final Random random = new Random();

    /**
     * Calculates anomaly scores for transactions.
     *
     * Higher score = more anomalous.
     */
    public List<Double> calculateAnomalyScores(
            List<Transaction> transactions) {

        List<Double> scores = new ArrayList<>();

        if (transactions == null || transactions.size() < 2) {
            return scores;
        }

        List<double[]> data = convertToFeatures(transactions);

        int sampleSize = Math.min(SAMPLE_SIZE, data.size());

        List<IsolationTree> trees = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_TREES; i++) {

            List<double[]> sample =
                    randomSample(data, sampleSize);

            int maxDepth =
                    (int) Math.ceil(
                            Math.log(sampleSize) / Math.log(2)
                    );

            IsolationTree tree =
                    new IsolationTree(
                            sample,
                            0,
                            maxDepth
                    );

            trees.add(tree);
        }

        for (double[] point : data) {

            double totalPathLength = 0;

            for (IsolationTree tree : trees) {
                totalPathLength += tree.pathLength(point);
            }

            double averagePathLength =
                    totalPathLength / NUMBER_OF_TREES;

            double normalization =
                    averagePathLength(sampleSize);

            double score =
                    Math.pow(
                            2,
                            -averagePathLength / normalization
                    );

            scores.add(score);
        }

        return scores;
    }

    /**
     * Converts transaction information into numerical ML features.
     *
     * Features:
     * 0 = transaction amount
     * 1 = transaction hour
     * 2 = account balance
     */
    private List<double[]> convertToFeatures(
            List<Transaction> transactions) {

        List<double[]> features = new ArrayList<>();

        for (Transaction transaction : transactions) {

            double amount =
                    transaction.getAmount() != null
                            ? transaction.getAmount()
                            : 0;

            double hour = 0;

            if (transaction.getTransactionTime() != null) {
                hour =
                        transaction.getTransactionTime().getHour();
            }

            double balance =
                    transaction.getAccountBalance() != null
                            ? transaction.getAccountBalance()
                            : 0;

            features.add(
                    new double[]{
                            amount,
                            hour,
                            balance
                    }
            );
        }

        return features;
    }

    /**
     * Randomly selects transactions for one isolation tree.
     */
    private List<double[]> randomSample(
            List<double[]> data,
            int sampleSize) {

        List<double[]> shuffled =
                new ArrayList<>(data);

        java.util.Collections.shuffle(
                shuffled,
                random
        );

        return new ArrayList<>(
                shuffled.subList(
                        0,
                        Math.min(sampleSize, shuffled.size())
                )
        );
    }

    /**
     * Average path length of unsuccessful search
     * in a binary search tree.
     */
    private double averagePathLength(int n) {

        if (n <= 1) {
            return 0;
        }

        if (n == 2) {
            return 1;
        }

        double harmonicNumber =
                Math.log(n)
                        + 0.5772156649;

        return 2 * harmonicNumber
                - (2.0 * (n - 1) / n);
    }

    /**
     * Isolation Tree.
     */
    private class IsolationTree {

        private final List<double[]> data;
        private final int depth;
        private final int maxDepth;

        private boolean externalNode;

        private int splitFeature;

        private double splitValue;

        private IsolationTree left;

        private IsolationTree right;

        IsolationTree(
                List<double[]> data,
                int depth,
                int maxDepth) {

            this.data = data;
            this.depth = depth;
            this.maxDepth = maxDepth;

            buildTree();
        }

        private void buildTree() {

            if (data.size() <= 1
                    || depth >= maxDepth) {

                externalNode = true;
                return;
            }

            int numberOfFeatures =
                    data.get(0).length;

            splitFeature =
                    random.nextInt(numberOfFeatures);

            double min =
                    Double.MAX_VALUE;

            double max =
                    -Double.MAX_VALUE;

            for (double[] point : data) {

                min =
                        Math.min(
                                min,
                                point[splitFeature]
                        );

                max =
                        Math.max(
                                max,
                                point[splitFeature]
                        );
            }

            if (min == max) {
                externalNode = true;
                return;
            }

            splitValue =
                    min + random.nextDouble()
                            * (max - min);

            List<double[]> leftData =
                    new ArrayList<>();

            List<double[]> rightData =
                    new ArrayList<>();

            for (double[] point : data) {

                if (point[splitFeature] < splitValue) {
                    leftData.add(point);
                } else {
                    rightData.add(point);
                }
            }

            if (leftData.isEmpty()
                    || rightData.isEmpty()) {

                externalNode = true;
                return;
            }

            left =
                    new IsolationTree(
                            leftData,
                            depth + 1,
                            maxDepth
                    );

            right =
                    new IsolationTree(
                            rightData,
                            depth + 1,
                            maxDepth
                    );
        }

        double pathLength(double[] point) {

            if (externalNode) {

                return depth
                        + averagePathLength(data.size());
            }

            if (point[splitFeature] < splitValue) {

                return left.pathLength(point);

            } else {

                return right.pathLength(point);
            }
        }
    }
}
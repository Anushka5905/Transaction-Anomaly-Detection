package com.anomaly.transactionanomalybackend.service;

import com.anomaly.transactionanomalybackend.dto.AnomalyResult;
import com.anomaly.transactionanomalybackend.dto.CsvUploadResult;
import com.anomaly.transactionanomalybackend.dto.DashboardStats;
import com.anomaly.transactionanomalybackend.ml.AnomalyDetector;
import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.model.Transaction;
import com.anomaly.transactionanomalybackend.model.User;
import com.anomaly.transactionanomalybackend.repository.AnomalyRepository;
import com.anomaly.transactionanomalybackend.repository.TransactionRepository;
import com.anomaly.transactionanomalybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;
    private final UserRepository userRepository;
    private final AnomalyDetector anomalyDetector;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public TransactionService(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            UserRepository userRepository,
            AnomalyDetector anomalyDetector,
            EmailService emailService,
            NotificationService notificationService) {

        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
        this.userRepository = userRepository;
        this.anomalyDetector = anomalyDetector;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    // ============================================================
    // NORMAL TRANSACTION CREATION
    // ============================================================

    public Transaction createTransaction(Transaction transaction) {

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        List<Transaction> allTransactions =
                transactionRepository.findAll();

        detectAndPersistAnomaly(
                savedTransaction,
                allTransactions
        );

        return savedTransaction;
    }

    // ============================================================
    // GET TRANSACTIONS
    // ============================================================

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // ============================================================
    // DELETE TRANSACTION
    // ============================================================

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    // ============================================================
    // GET ANOMALIES
    // ============================================================

    public List<AnomalyResult> getAnomalies() {

        List<Transaction> transactions =
                transactionRepository.findAll();

        return anomalyDetector.analyzeTransactions(transactions);
    }

    // ============================================================
    // DASHBOARD STATISTICS
    // ============================================================

    public DashboardStats getDashboardStats() {

        long totalTransactions =
                transactionRepository.count();

        long totalAnomalies =
                anomalyRepository.count();

        long highRiskCount =
                anomalyRepository
                        .findByStatus("HIGH_RISK")
                        .size();

        long suspiciousCount =
                anomalyRepository
                        .findByStatus("SUSPICIOUS")
                        .size();

        double averageRiskScore =
                anomalyRepository.findAll()
                        .stream()
                        .mapToDouble(anomaly ->
                                anomaly.getRiskScore() != null
                                        ? anomaly.getRiskScore()
                                        : 0.0
                        )
                        .average()
                        .orElse(0.0);

        return new DashboardStats(
                totalTransactions,
                totalAnomalies,
                highRiskCount,
                suspiciousCount,
                averageRiskScore
        );
    }

    // ============================================================
    // CSV UPLOAD
    // ============================================================

    public CsvUploadResult uploadCsv(MultipartFile file) {

        CsvUploadResult result = new CsvUploadResult();

        if (file == null || file.isEmpty()) {
            result.addError("CSV file is empty.");
            return result;
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".csv")) {

            result.addError("Only CSV files are allowed.");
            return result;
        }

        String expectedHeader =
                "userid,amount,transactiontype,location,merchant,paymentmethod,transactiontime,accountbalance";

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     file.getInputStream(),
                                     StandardCharsets.UTF_8))) {

            String header = reader.readLine();

            if (header == null || header.isBlank()) {
                result.addError("CSV file does not contain a header.");
                return result;
            }

            String normalizedHeader =
                    header.replace("\uFEFF", "")
                            .trim()
                            .toLowerCase()
                            .replace(" ", "");

            if (!normalizedHeader.equals(expectedHeader)) {

                result.addError(
                        "Invalid CSV header. Expected: " +
                                "userId,amount,transactionType,location,merchant,paymentMethod,transactionTime,accountBalance"
                );

                return result;
            }

            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {

                rowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                result.setTotalRows(
                        result.getTotalRows() + 1
                );

                try {

                    List<String> columns =
                            parseCsvLine(line);

                    if (columns.size() != 8) {
                        throw new IllegalArgumentException(
                                "Expected 8 columns but found "
                                        + columns.size()
                        );
                    }

                    Transaction transaction =
                            createTransactionFromCsv(columns);

                    createTransaction(transaction);

                    result.setImportedRows(
                            result.getImportedRows() + 1
                    );

                } catch (Exception e) {

                    result.setFailedRows(
                            result.getFailedRows() + 1
                    );

                    result.addError(
                            "Row " + rowNumber + ": "
                                    + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {

            result.addError(
                    "Failed to read CSV file: "
                            + e.getMessage()
            );
        }

        return result;
    }

    // ============================================================
    // CREATE TRANSACTION FROM CSV ROW
    // ============================================================

    private Transaction createTransactionFromCsv(
            List<String> columns) {

        String userIdText = columns.get(0).trim();
        String amountText = columns.get(1).trim();
        String transactionType = columns.get(2).trim();
        String location = columns.get(3).trim();
        String merchant = columns.get(4).trim();
        String paymentMethod = columns.get(5).trim();
        String transactionTimeText = columns.get(6).trim();
        String accountBalanceText = columns.get(7).trim();

        if (userIdText.isBlank()) {
            throw new IllegalArgumentException(
                    "userId is required"
            );
        }

        if (amountText.isBlank()) {
            throw new IllegalArgumentException(
                    "amount is required"
            );
        }

        if (transactionType.isBlank()) {
            throw new IllegalArgumentException(
                    "transactionType is required"
            );
        }

        if (transactionTimeText.isBlank()) {
            throw new IllegalArgumentException(
                    "transactionTime is required"
            );
        }

        long userId;

        try {
            userId = Long.parseLong(userIdText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "userId must be a number"
            );
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "amount must be a number"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than 0"
            );
        }

        LocalDateTime transactionTime;

        try {
            transactionTime =
                    LocalDateTime.parse(
                            transactionTimeText
                    );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "transactionTime must use format " +
                            "YYYY-MM-DDTHH:MM:SS"
            );
        }

        double accountBalance = 0.0;

        if (!accountBalanceText.isBlank()) {

            try {
                accountBalance =
                        Double.parseDouble(
                                accountBalanceText
                        );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "accountBalance must be a number"
                );
            }

            if (accountBalance < 0) {
                throw new IllegalArgumentException(
                        "accountBalance cannot be negative"
                );
            }
        }

        Transaction transaction =
                new Transaction();

        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setLocation(location);
        transaction.setMerchant(merchant);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setTransactionTime(transactionTime);
        transaction.setAccountBalance(accountBalance);

        return transaction;
    }

    // ============================================================
    // CSV PARSER
    // ============================================================

    private List<String> parseCsvLine(String line) {

        List<String> columns = new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (character == '"') {

                if (insideQuotes &&
                        i + 1 < line.length() &&
                        line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (character == ',' &&
                    !insideQuotes) {

                columns.add(
                        current.toString().trim()
                );

                current.setLength(0);

            } else {

                current.append(character);
            }
        }

        columns.add(
                current.toString().trim()
        );

        return columns;
    }

    // ============================================================
    // ANOMALY DETECTION
    // + NOTIFICATION
    // + EMAIL FOR HIGH RISK
    // ============================================================

    private void detectAndPersistAnomaly(
            Transaction currentTransaction,
            List<Transaction> allTransactions) {

        List<AnomalyResult> results =
                anomalyDetector.analyzeTransactions(
                        allTransactions
                );

        for (AnomalyResult result : results) {

            Transaction detectedTransaction =
                    result.getTransaction();

            if (!detectedTransaction.getId()
                    .equals(currentTransaction.getId())) {

                continue;
            }

            if (anomalyRepository
                    .existsByTransactionId(
                            currentTransaction.getId())) {

                continue;
            }

            Anomaly anomaly =
                    new Anomaly(
                            currentTransaction.getId(),
                            currentTransaction.getUserId(),
                            result.getMlScore(),
                            result.getRuleScore(),
                            result.getRiskScore(),
                            result.getStatus(),
                            String.join(
                                    ", ",
                                    result.getReasons()
                            ),
                            LocalDateTime.now()
                    );

            Anomaly savedAnomaly =
                    anomalyRepository.save(anomaly);

            String status =
                    savedAnomaly.getStatus();

            // ====================================================
            // NOTIFICATION FOR SUSPICIOUS + HIGH RISK
            // ====================================================

            if ("HIGH_RISK".equals(status)
                    || "SUSPICIOUS".equals(status)) {

                String notificationMessage;

                if ("HIGH_RISK".equals(status)) {

                    notificationMessage =
                            "High-risk transaction detected: TX-"
                                    + currentTransaction.getId()
                                    + " with amount ₹"
                                    + currentTransaction.getAmount()
                                    + ". Risk score: "
                                    + Math.round(
                                    savedAnomaly.getRiskScore()
                            );

                } else {

                    notificationMessage =
                            "Suspicious transaction detected: TX-"
                                    + currentTransaction.getId()
                                    + " with amount ₹"
                                    + currentTransaction.getAmount()
                                    + ". Risk score: "
                                    + Math.round(
                                    savedAnomaly.getRiskScore()
                            );
                }

                // ====================================================
                // CREATE PERSONAL NOTIFICATION FOR ALL SYSTEM USERS
                // ====================================================

                List<User> users = userRepository.findAll();

                for (User user : users) {

                    if (user.getEmail() == null ||
                            user.getEmail().isBlank()) {
                        continue;
                    }

                    String role = user.getRole();

                    if ("ADMIN".equalsIgnoreCase(role)
                            || "ANALYST".equalsIgnoreCase(role)) {

                        notificationService.createNotification(
                                user.getEmail(),
                                notificationMessage,
                                status,
                                currentTransaction.getId(),
                                savedAnomaly.getId()
                        );
                    }
                }
            }

            // ====================================================
            // EMAIL ONLY FOR HIGH RISK
            // ====================================================

            if ("HIGH_RISK".equals(status)) {

                emailService.sendHighRiskAlert(
                        currentTransaction,
                        savedAnomaly
                );
            }
        }
    }
}
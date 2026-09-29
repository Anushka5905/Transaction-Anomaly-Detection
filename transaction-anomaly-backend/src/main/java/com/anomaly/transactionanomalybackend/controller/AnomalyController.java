package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.AnomalyWithTransaction;
import com.anomaly.transactionanomalybackend.model.Transaction;
import com.anomaly.transactionanomalybackend.repository.TransactionRepository;
import com.anomaly.transactionanomalybackend.model.Anomaly;
import com.anomaly.transactionanomalybackend.repository.AnomalyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyRepository anomalyRepository;
    private final TransactionRepository transactionRepository;

    public AnomalyController(
        AnomalyRepository anomalyRepository,
        TransactionRepository transactionRepository) {

    this.anomalyRepository = anomalyRepository;
    this.transactionRepository = transactionRepository;
}

@GetMapping
public ResponseEntity<List<AnomalyWithTransaction>> getAllAnomalies() {

    List<Anomaly> anomalies =
            anomalyRepository.findAll();

    List<AnomalyWithTransaction> result =
            anomalies.stream()
                    .map(anomaly -> {

                        Transaction transaction =
                                transactionRepository
                                        .findById(
                                                anomaly.getTransactionId()
                                        )
                                        .orElse(null);

                        return new AnomalyWithTransaction(
                                anomaly,
                                transaction
                        );
                    })
                    .toList();

    return ResponseEntity.ok(result);
}

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Anomaly>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                anomalyRepository.findByStatus(status)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Anomaly>> getByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                anomalyRepository.findByUserId(userId)
        );
    }
}